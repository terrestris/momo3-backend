/* eslint-disable max-len */

/**
 * Metadata Util
 *
 * Write and read XML
 */
Ext.define('MoMo.shared.MetadataUtil', {

    requires: [
        'Ext.util.Format'
    ],

    statics: {
        /**
         *
         */
        keyXpathMapper: {
            title: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString',
            abstract: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString',
            organisation: {
                name: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString',
                address: {
                    deliveryPoint: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString',
                    city: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString',
                    postalCode: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString',
                    country: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString'
                },
                website: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gco:CharacterString'
            },
            person: {
                name: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString',
                email: 'gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString'
            },
            referenceDate: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date',
            topic: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode',
            geography: {
                extent: {
                    minX: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal',
                    minY: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal',
                    maxX: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal',
                    maxY: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal'
                },
                projection: 'gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString'
            },
            timeExtent: {
                start: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition',
                end: 'gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition'
            },
            format: 'gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gco:CharacterString',
            limitations: 'gmd:metadataConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString',
            onlineResource: 'gmd:dataSetURI:/gco:CharacterString',
            dataSource: null,
            publications: null
        },

        /**
         *
         */
        parseXml: function(xmlString){
            var parser = new DOMParser();
            var xmlDoc = parser.parseFromString(xmlString, "text/xml");
            return xmlDoc;
        },

        /**
         *
         */
        sendCswRequest: function(xmlString, successCallBack){
            Ext.Ajax.request({
                url: BasiGX.util.Url.getWebProjectBaseUrl() +
                    'metadata/csw.action',
                method: "POST",
                params: {
                    xml: xmlString
                },
                defaultHeaders: BasiGX.util.CSRF.getHeader(),
                scope: this,
                success: successCallBack
            });
        },

        /**
         *
         */
        getInsertBlankXml: function(){
            var xml;
            Ext.Ajax.request({
                url: BasiGX.util.Url.getWebProjectBaseUrl() + 'admin/resources/data/xmlTemplates/insertMetadata.xml',
                method: 'GET',
                async: false,
                success: function(response) {
                    xml = response.responseText;
                }
            });
            return xml;
        },

        /**
         *
         */
        getUpdateXml: function(uuid, metadata){
            var recordsString = this.valuesToXmlString(metadata);

            var xml = '<?xml version="1.0" encoding="UTF-8"?>' +
                '<csw:Transaction service="CSW" version="2.0.2" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0">' +
                '  <csw:Update>' +
                recordsString +
                '    <csw:Constraint version="1.1.0">' +
                '         <ogc:Filter>' +
                '            <ogc:PropertyIsEqualTo>' +
                '                 <ogc:PropertyName>Identifier</ogc:PropertyName>' +
                '                 <ogc:Literal>' + uuid + '</ogc:Literal>' +
                '             </ogc:PropertyIsEqualTo>' +
                '         </ogc:Filter>' +
                '     </csw:Constraint>' +
                '  </csw:Update>' +
                '</csw:Transaction>';
            return xml;
        },

        /**
         *
         */
        valuesToXmlString: function(metadata, prefix){
            var me = this;
            var recordsString = '';

            Ext.Object.each(metadata, function(k, v){
                if(metadata[k]){
                    var key = prefix ? prefix + '.' + k : k;
                    if(!Ext.isObject(metadata[k])){
                        recordsString += me.getRecordXml(key,v);
                    } else {
                        recordsString += me.valuesToXmlString(v, key);
                    }
                }
            });

            return recordsString;
        },

        /**
         *
         */
        getRecordXml: function(selector, value){
            if(Ext.String.endsWith(selector, '.referenceDate') ||
                    Ext.String.endsWith(selector, '.start') ||
                    Ext.String.endsWith(selector, '.end')){
                value = this.transformDateString(value);
            }

            return '<csw:RecordProperty>' +
                '<csw:Name>' + this.objectValueFromSelector(this.keyXpathMapper, selector) + '</csw:Name>' +
                '<csw:Value>' + Ext.util.Format.htmlEncode(value) + '</csw:Value>' +
            '</csw:RecordProperty>';
        },

        /**
         * From stack overflow: http://stackoverflow.com/a/6491621
         */
        objectValueFromSelector: function(o, s) {
            s = s.replace(/\[(\w+)\]/g, '.$1');
            s = s.replace(/^\./, '');
            var a = s.split('.');
            for (var i = 0, n = a.length; i < n; ++i) {
                var k = a[i];
                if (k in o) {
                    o = o[k];
                } else {
                    return;
                }
            }
            return o;
        },

        /**
         *
         */
        getLoadXml: function(uuid){
            var xml = '<?xml version="1.0"?>' +
                '<csw:GetRecordById xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" outputSchema="http://www.isotc211.org/2005/gmd">' +
                    '<csw:Id>' + uuid + '</csw:Id>' +
                    '<csw:ElementSetName>full</csw:ElementSetName>' +
                '</csw:GetRecordById>';
            return xml;
        },

        /**
         *
         */
        uuidFromXmlString: function(xmlString){
            var xml = this.parseXml(xmlString);
            var identifierNodes = xml.getElementsByTagName('identifier');
            var uuid;
            if(!Ext.isEmpty(identifierNodes) && identifierNodes[0] &&
                identifierNodes[0].innerHTML){
                    uuid = identifierNodes[0].innerHTML;
            }
            return uuid;
        },

        /**
         *
         */
        parseMetadataXml: function(xmlString){
            var xml = this.parseXml(xmlString);
            var metadata = {
                title: this.getTextContentsViaQuerySelector(xml, 'title > CharacterString')[0],
                abstract: this.getTextContentsViaQuerySelector(xml, 'abstract > CharacterString')[0],
                organisation: {
                    name: this.getTextContentsViaQuerySelector(xml, 'organisationName > CharacterString')[0],
                    address: {
                        deliveryPoint: this.getTextContentsViaQuerySelector(xml, 'deliveryPoint > CharacterString')[0],
                        city: this.getTextContentsViaQuerySelector(xml, 'city > CharacterString')[0],
                        postalCode: this.getTextContentsViaQuerySelector(xml, 'postalCode > CharacterString')[0],
                        country: this.getTextContentsViaQuerySelector(xml, 'country > CharacterString')[0]
                    },
                    website: this.getTextContentsViaQuerySelector(xml, 'linkage > CharacterString')[0]
                },
                person: {
                    name: this.getTextContentsViaQuerySelector(xml, 'individualName > CharacterString')[0],
                    email: this.getTextContentsViaQuerySelector(xml, 'electronicMailAddress > CharacterString')[0]
                },
                referenceDate: this.transformDateString(this.getTextContentsViaQuerySelector(xml, 'CI_Date > date > Date')[0]),
                topic: this.getTextContentsViaQuerySelector(xml, 'topicCategory > MD_TopicCategoryCode')[0],
                geography: {
                    extent: {
                        minX: this.getTextContentsViaQuerySelector(xml, 'westBoundLongitude > Decimal')[0],
                        minY: this.getTextContentsViaQuerySelector(xml, 'southBoundLatitude > Decimal')[0],
                        maxX: this.getTextContentsViaQuerySelector(xml, 'eastBoundLongitude > Decimal')[0],
                        maxY: this.getTextContentsViaQuerySelector(xml, 'northBoundLatitude > Decimal')[0]
                    },
                    projection: this.getTextContentsViaQuerySelector(xml, 'codeSpace > CharacterString')[0]
                },
                timeExtent: {
                    start: this.getTextContentsViaQuerySelector(xml, 'TimePeriod > beginPosition')[0],
                    end: this.getTextContentsViaQuerySelector(xml, 'TimePeriod > endPosition')[0]
                },
                format: this.getTextContentsViaQuerySelector(xml, 'distributionFormat > CharacterString')[0],
                limitations: this.getTextContentsViaQuerySelector(xml, 'useLimitation > CharacterString')[0],
                onlineResource: this.getTextContentsViaQuerySelector(xml, 'dataSetURI > CharacterString')[0],
                dataSource: null,
                publications: null
            };

            return metadata;
        },

        /**
         *
         */
        transformDateString: function(dateString){
            var date = new Date(dateString);
            return Ext.Date.format(date, "Y-m-d");
        },

        /**
         *
         */
        getTextContentsViaQuerySelector: function(xml, selectorString) {
            if(!xml){
                throw "No xml document given.";
            }
            if(!selectorString){
                throw "No selectorString given.";
            }

            var elements = xml.documentElement.querySelectorAll(selectorString);
            if(!Ext.isEmpty(elements)){
                return Ext.Array.pluck(elements, "textContent");
            } else {
                return null;
            }
        }
    }

});

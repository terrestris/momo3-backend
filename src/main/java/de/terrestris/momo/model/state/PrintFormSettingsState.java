package de.terrestris.momo.model.state;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.ReadableDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.terrestris.shogun2.model.PersistentObject;

/**
*
* @author Johannes Weskamm
* @author terrestris GmbH & Co. KG
*
*/
@Entity
@Table
public class PrintFormSettingsState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String appCombo;

	/**
	 *
	 */
	private String description;

	/**
	 *
	 */
	private Integer dpi;

	/**
	 *
	 */
	private String format;

	/**
	 *
	 */
	private String layout;

	/**
	 *
	 */
	private String legend;

	/**
	 *
	 */
	private String mapNumber;

	/**
	 *
	 */
	private String northArrowDef;

	/**
	 *
	 */
	private String scalebar;

	/**
	 *
	 */
	private String title;

	/**
	 *
	 */
	private String titleAuthorName;

	/**
	 *
	 */
	private String titleCoordSystemString;

	/**
	 *
	 */
	private String titleDataSource;

	/**
	 *
	 */
	private String titleDate;

	/**
	 *
	 */
	private String titleDatumString;

	/**
	 *
	 */
	private String titleProjString;

	/**
	 *
	 */
	private String titleScale;


	/**
	 *
	 */
	public PrintFormSettingsState() {
	}

	/**
	 * Overwrite this getter to set the {@link JsonIgnore} value to false
	 * for this subclass.
	 */
	@Override
	@JsonIgnore(false)
	public ReadableDateTime getCreated() {
		return super.getCreated();
	}

	/**
	 * Overwrite this getter to set the {@link JsonIgnore} value to false
	 * for this subclass.
	 */
	@Override
	@JsonIgnore(false)
	public ReadableDateTime getModified() {
		return super.getModified();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the appCombo
	 */
	public String getAppCombo() {
		return appCombo;
	}

	/**
	 * @param appCombo the appCombo to set
	 */
	public void setAppCombo(String appCombo) {
		this.appCombo = appCombo;
	}

	/**
	 * @return the dpi
	 */
	public Integer getDpi() {
		return dpi;
	}

	/**
	 * @param dpi the dpi to set
	 */
	public void setDpi(Integer dpi) {
		this.dpi = dpi;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the layout
	 */
	public String getLayout() {
		return layout;
	}

	/**
	 * @param layout the layout to set
	 */
	public void setLayout(String layout) {
		this.layout = layout;
	}

	/**
	 * @return the legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * @param legend the legend to set
	 */
	public void setLegend(String legend) {
		this.legend = legend;
	}

	/**
	 * @return the mapNumber
	 */
	public String getMapNumber() {
		return mapNumber;
	}

	/**
	 * @param mapNumber the mapNumber to set
	 */
	public void setMapNumber(String mapNumber) {
		this.mapNumber = mapNumber;
	}

	/**
	 * @return the northArrowDef
	 */
	public String getNorthArrowDef() {
		return northArrowDef;
	}

	/**
	 * @param northArrowDef the northArrowDef to set
	 */
	public void setNorthArrowDef(String northArrowDef) {
		this.northArrowDef = northArrowDef;
	}

	/**
	 * @return the scalebar
	 */
	public String getScalebar() {
		return scalebar;
	}

	/**
	 * @param scalebar the scalebar to set
	 */
	public void setScalebar(String scalebar) {
		this.scalebar = scalebar;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the titleAuthorName
	 */
	public String getTitleAuthorName() {
		return titleAuthorName;
	}

	/**
	 * @param titleAuthorName the titleAuthorName to set
	 */
	public void setTitleAuthorName(String titleAuthorName) {
		this.titleAuthorName = titleAuthorName;
	}

	/**
	 * @return the titleCoordSystemString
	 */
	public String getTitleCoordSystemString() {
		return titleCoordSystemString;
	}

	/**
	 * @param titleCoordSystemString the titleCoordSystemString to set
	 */
	public void setTitleCoordSystemString(String titleCoordSystemString) {
		this.titleCoordSystemString = titleCoordSystemString;
	}

	/**
	 * @return the titleDataSource
	 */
	public String getTitleDataSource() {
		return titleDataSource;
	}

	/**
	 * @param titleDataSource the titleDataSource to set
	 */
	public void setTitleDataSource(String titleDataSource) {
		this.titleDataSource = titleDataSource;
	}

	/**
	 * @return the titleDate
	 */
	public String getTitleDate() {
		return titleDate;
	}

	/**
	 * @param titleDate the titleDate to set
	 */
	public void setTitleDate(String titleDate) {
		this.titleDate = titleDate;
	}

	/**
	 * @return the titleDatumString
	 */
	public String getTitleDatumString() {
		return titleDatumString;
	}

	/**
	 * @param titleDatumString the titleDatumString to set
	 */
	public void setTitleDatumString(String titleDatumString) {
		this.titleDatumString = titleDatumString;
	}

	/**
	 * @return the titleProjString
	 */
	public String getTitleProjString() {
		return titleProjString;
	}

	/**
	 * @param titleProjString the titleProjString to set
	 */
	public void setTitleProjString(String titleProjString) {
		this.titleProjString = titleProjString;
	}

	/**
	 * @return the titleScale
	 */
	public String getTitleScale() {
		return titleScale;
	}

	/**
	 * @param titleScale the titleScale to set
	 */
	public void setTitleScale(String titleScale) {
		this.titleScale = titleScale;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 1627).appendSuper(super.hashCode())
				.append(getDescription())
				.append(getAppCombo())
				.append(getDpi())
				.append(getFormat())
				.append(getLayout())
				.append(getLegend())
				.append(getMapNumber())
				.append(getNorthArrowDef())
				.append(getScalebar())
				.append(getTitle())
				.append(getTitleAuthorName())
				.append(getTitleCoordSystemString())
				.append(getTitleDataSource())
				.append(getTitleDate())
				.append(getTitleDatumString())
				.append(getTitleProjString())
				.append(getTitleScale())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PrintFormSettingsState))
			return false;
		PrintFormSettingsState other = (PrintFormSettingsState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))

				.append(getDescription(), other.getDescription())
				.append(getAppCombo(), other.getAppCombo())
				.append(getDpi(), other.getDescription())
				.append(getFormat(), other.getFormat())
				.append(getLayout(), other.getLayout())
				.append(getLegend(), other.getLegend())
				.append(getMapNumber(), other.getMapNumber())
				.append(getNorthArrowDef(), other.getNorthArrowDef())
				.append(getScalebar(), other.getScalebar())
				.append(getTitle(), other.getTitle())
				.append(getTitleAuthorName(), other.getTitleAuthorName())
				.append(getTitleCoordSystemString(), other.getTitleCoordSystemString())
				.append(getTitleDataSource(), other.getTitleDataSource())
				.append(getTitleDate(), other.getTitleDate())
				.append(getTitleDatumString(), other.getTitleDatumString())
				.append(getTitleProjString(), other.getTitleProjString())
				.append(getTitleScale(), other.getTitleScale())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("description", getDescription())
				.append("appCombo", getAppCombo())
				.append("dpi", getDpi())
				.append("format", getFormat())
				.append("layout", getLayout())
				.append("legend", getLegend())
				.append("mapNumber", getMapNumber())
				.append("northArrowDef", getNorthArrowDef())
				.append("scalebar", getScalebar())
				.append("title", getTitle())
				.append("titleAuthorName", getTitleAuthorName())
				.append("titleCoordSystemString", getTitleCoordSystemString())
				.append("titleDataSource", getTitleDataSource())
				.append("titleDate", getTitleDate())
				.append("titleDatumString", getTitleDatumString())
				.append("titleProjString", getTitleProjString())
				.append("titleScale", getTitleScale())
				.toString();
	}

}

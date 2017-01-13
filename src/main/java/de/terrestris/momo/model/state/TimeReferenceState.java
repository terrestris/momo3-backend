package de.terrestris.momo.model.state;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.ReadableDateTime;

import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class TimeReferenceState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private ReadableDateTime dateInstant;

	/**
	 *
	 */
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private ReadableDateTime dateRangeStart;

	/**
	 *
	 */
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private ReadableDateTime dateRangeEnd;

	/**
	 *
	 */
	private String netDefinition;

	/**
	 *
	 */
	public TimeReferenceState() {
	}

	/**
	 *
	 * @param dateInstant
	 * @param dateRangeStart
	 * @param dateRangeEnd
	 * @param netDefinition
	 */
	public TimeReferenceState(ReadableDateTime dateInstant,
			ReadableDateTime dateRangeStart, ReadableDateTime dateRangeEnd,
			String netDefinition) {
		super();
		this.dateInstant = dateInstant;
		this.dateRangeStart = dateRangeStart;
		this.dateRangeEnd = dateRangeEnd;
		this.netDefinition = netDefinition;
	}

	/**
	 * @return the dateInstant
	 */
	public ReadableDateTime getDateInstant() {
		return dateInstant;
	}

	/**
	 * @param dateInstant the dateInstant to set
	 */
	public void setDateInstant(ReadableDateTime dateInstant) {
		this.dateInstant = dateInstant;
	}

	/**
	 * @return the dateRangeStart
	 */
	public ReadableDateTime getDateRangeStart() {
		return dateRangeStart;
	}

	/**
	 * @param dateRangeStart the dateRangeStart to set
	 */
	public void setDateRangeStart(ReadableDateTime dateRangeStart) {
		this.dateRangeStart = dateRangeStart;
	}

	/**
	 * @return the dateRangeEnd
	 */
	public ReadableDateTime getDateRangeEnd() {
		return dateRangeEnd;
	}

	/**
	 * @param dateRangeEnd the dateRangeEnd to set
	 */
	public void setDateRangeEnd(ReadableDateTime dateRangeEnd) {
		this.dateRangeEnd = dateRangeEnd;
	}

	/**
	 * @return the netDefinition
	 */
	public String getNetDefinition() {
		return netDefinition;
	}

	/**
	 * @param netDefinition the netDefinition to set
	 */
	public void setNetDefinition(String netDefinition) {
		this.netDefinition = netDefinition;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 73).appendSuper(super.hashCode())
				.append(getDateInstant())
				.append(getDateRangeStart())
				.append(getDateRangeEnd())
				.append(getNetDefinition())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimeReferenceState))
			return false;
		TimeReferenceState other = (TimeReferenceState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getDateInstant(), other.getDateInstant())
				.append(getDateRangeStart(), other.getDateRangeStart())
				.append(getDateRangeEnd(), other.getDateRangeEnd())
				.append(getNetDefinition(), other.getNetDefinition())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("dateInstant", getDateInstant())
				.append("dateRangeStart", getDateRangeStart())
				.append("dateRangeEnd", getDateRangeEnd())
				.append("netDefinition", getNetDefinition())
				.toString();
	}

}

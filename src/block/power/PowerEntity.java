package block.power;

public interface PowerEntity {
	/** Get the maximum rate at which power can flow out of the entity, should be positive */
	double getMaxRate();
	/** Get the maximum rate at which power can flow into the entity, should be negative */
	double getMinRate();
	/** Get the desired rate at which power should flow in / out of the device */
	double getDesiredRate();
	/** Input or output power to the device */
	double flow(double value);
	/** Get the importance of satisfying the desired rate */
	double importance();
}

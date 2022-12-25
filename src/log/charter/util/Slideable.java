package log.charter.util;

public interface Slideable {
	abstract public Integer slideTo();

	abstract public boolean unpitched();

	abstract public void setSlide(Integer slideTo, boolean unpitched);
}

package com.lunatech.drools.traits.model;

/**
 * A natural number (positive whole number).
 */
public class NaturalNumber implements Comparable<NaturalNumber> {

    private Long value;

    public NaturalNumber() {
    }

    public NaturalNumber(final Long value) {
        setValue(value);
    }

    public NaturalNumber(final int value) {
        setValue(Long.valueOf(value));
    }

    public void setValue(Long value) {
        if (value < 1) {
            throw new IllegalArgumentException("Not a natural number: " + value);
        }
        this.value = value;
    }

    @Override
    public int compareTo(NaturalNumber number) {
        return value.compareTo(number.getValue());
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

package com.goodgame.profiling.commons.statistics.units.format;

import java.util.List;

import com.goodgame.profiling.commons.statistics.units.Scales;

public abstract class WithSubUnitsFormatter<T extends Enum<T> & Scales> implements UnitFormatter {
    private final int nrComponents;
    private final T inputUnit;
    private final List<T> allUnits;
    private final String appendToAll;

    public WithSubUnitsFormatter(int nrComponents, T inputUnit, List<T> allUnits, String appendToAll) {
        this.nrComponents = nrComponents;
        this.inputUnit = inputUnit;
        this.allUnits = allUnits;
        this.appendToAll = appendToAll;
    }

    public WithSubUnitsFormatter(int nrComponents, T inputUnit, List<T> allUnits) {
        this(nrComponents, inputUnit, allUnits, "");
    }

    private T getAppropriatePrefix(double value) {
        for (T t : allUnits) {
            if (value / t.getMultiplier() >= 1) {
                return t;
            }
        }
        
        return null;
    }

    private T nextEnumValue(T t) {
        return allUnits.get(t.ordinal() + 1);
    }

    private boolean hasNextEnumValue(T t) {
        return t.ordinal() + 1 < allUnits.size();
    }

    protected abstract String onNoSubunitsLeft(double remainingValue, int remainingComponents);

    @Override
    public String format(double value) {
        StringBuilder sb = new StringBuilder();

        if (value < 0) {
            value *= -1;
            sb.append('-');
        }
        
        double normalizedValue = value * inputUnit.getMultiplier();

        T outputUnit = getAppropriatePrefix(normalizedValue);
        int i = nrComponents;
        if (outputUnit != null) {
            while (i > 0) {
                double scaledValue = normalizedValue / outputUnit.getMultiplier();

                int integerpart = (int) scaledValue;

                sb.append(integerpart + outputUnit.getSymbol() + appendToAll + " ");

                normalizedValue -= integerpart * outputUnit.getMultiplier();

                i--;
                if (hasNextEnumValue(outputUnit) && normalizedValue > 0) {
                    outputUnit = nextEnumValue(outputUnit);
                } else {
                    break;
                }
            }
        }
        if (i > 0) {
            sb.append(onNoSubunitsLeft(normalizedValue, i));
        }

        return sb.toString();
    }

}

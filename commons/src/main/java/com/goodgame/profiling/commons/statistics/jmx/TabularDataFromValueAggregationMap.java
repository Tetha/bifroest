package com.goodgame.profiling.commons.statistics.jmx;

import java.util.Map;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;

public class TabularDataFromValueAggregationMap {

	private final Map<String, ? extends ValueAggregation> mapping;

	private String rowTypeName;
	private String rowTypeDescrption;

	private String keyName;
	private String valueName;

	private String keyDescription;
	private String valueDescription;

	private String tableTypeName;
	private String tableTypeDescription;

	public TabularDataFromValueAggregationMap( Map<String, ? extends ValueAggregation> mapping ) {
		this.mapping = mapping;
	}

	public TabularDataFromValueAggregationMap withRowTypeCalled( String rowTypeName ) {
		this.rowTypeName = rowTypeName;
		return this;
	}

	public TabularDataFromValueAggregationMap withRowTypeDescribedAs( String rowTypeDescription ) {
		this.rowTypeDescrption = rowTypeDescription;
		return this;
	}

	public TabularDataFromValueAggregationMap withTableTypeCalled( String tableTypeName ) {
		this.tableTypeName = tableTypeName;
		return this;
	}

	public TabularDataFromValueAggregationMap withTableTypeDescribedAs( String tableTypeDescription ) {
		this.tableTypeDescription = tableTypeDescription;
		return this;
	}

	public TabularDataFromValueAggregationMap withKeysCalled( String keyName ) {
		this.keyName = keyName;
		return this;
	}

	public TabularDataFromValueAggregationMap withKeysDescribedAs( String keyDescription ) {
		this.keyDescription = keyDescription;
		return this;
	}

	public TabularDataFromValueAggregationMap withValuesCalled( String valueName ) {
		this.valueName = valueName;
		return this;
	}

	public TabularDataFromValueAggregationMap withValuesDescribedAs( String valueDescription ) {
		this.valueDescription = valueDescription;
		return this;
	}

	public TabularData buildData() throws OpenDataException {
		CompositeType rowType = new CompositeType( rowTypeName, rowTypeDescrption, new String[] { keyName, valueName }, new String[] { keyDescription,
				valueDescription }, new OpenType<?>[] { SimpleType.STRING, SimpleType.DOUBLE } );

		TabularType tableType = new TabularType( tableTypeName, tableTypeDescription, rowType, new String[] { keyName } );

		TabularData result = new TabularDataSupport( tableType );
		convertValueAggregationToTable( rowType, result );
		return result;
	}

	private void convertValueAggregationToTable( CompositeType rowType, TabularData result ) throws OpenDataException {
		for ( Map.Entry<String, ? extends ValueAggregation> entry : mapping.entrySet() ) {
			result.put( new CompositeDataSupport( rowType, new String[] { keyName, valueName }, new Object[] { entry.getKey(),
					entry.getValue().getAggregatedValue() } ) );
		}
	}

}
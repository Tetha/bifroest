package com.goodgame.profiling.graphite_aggregator.systems.aggregation;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.aggregation.LastAggregation;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusImpl;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.graphite_aggregator.systems.AggregatorEnvironment;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class AggregatorTaskRealLifeTest {
    private static final String METRIC_NAME = "server.com.ggs-net.de.services.test-graphite-web01.System.CPU.user";

    @Mock private RetentionConfiguration retentions;
    @Mock private CassandraAccessLayer database;
    @Mock private AggregatorEnvironment environment;

    private RetentionStrategy strategy;
    private RetentionLevel nextLevel;
    private RetentionTable sourceTable;
    private RetentionTable nextTable;

    @Before
    public void createMocks() {
        EventBusManager.setEventBus( new EventBusImpl() );

        MockitoAnnotations.initMocks( this );
        when( environment.cassandraAccessLayer() ).thenReturn( database );
        when( environment.retentions() ).thenReturn( retentions );
        when( retentions.findFunctionForMetric( METRIC_NAME ) ).thenAnswer( i -> new LastAggregation() );

        // Levels and Strategy
        RetentionLevel level = new RetentionLevel( "3daT5m", 5 * 60, 3, 24 * 60 * 60 );
        nextLevel = new RetentionLevel( "4daT1h", 60 * 60, 2, 4 * 24 * 60 * 60 );
        strategy = new RetentionStrategy( "3da1w", 2, Arrays.asList( level, nextLevel ), 0, 0, 0 );
        sourceTable = new RetentionTable( strategy, level, 16324 );
        nextTable = new RetentionTable( strategy, nextLevel, 4081 );

        // Names
        when( database.loadMetricNames( sourceTable ) ).thenReturn( Arrays.asList( METRIC_NAME ) );

        when( database.loadUnorderedMetrics( sourceTable, METRIC_NAME ) ).thenReturn( Arrays.asList( metricsInSource ) );
        when( database.loadTables() ).thenReturn( Arrays.asList( sourceTable ) );
    }

    @Test
    public void test() {
        new AggregatorTask<AggregatorEnvironment>( environment, sourceTable, nextLevel ).run();

        verify( database, atLeastOnce() ).createTableIfNecessary( nextTable );
        MetricMatcher m = new MetricMatcher(
                IntStream.range( 0, 24 ).mapToObj( i -> metricsInSource[12 * i + 11] ).collect( Collectors.toList() ) );
        verify( database, atLeastOnce() ).insertMetrics( eq( nextTable ), argThat( m ) );
    }

    private class MetricMatcher extends ArgumentMatcher<Collection<Metric>> {
        private final Set<Metric> expected;

        private MetricMatcher( Collection<Metric> expected ) {
            this.expected = new HashSet<>( expected );
        }

        @Override
        public boolean matches( Object argument ) {
            if ( argument instanceof Collection ) {
                @SuppressWarnings( "unchecked" )
                Collection<Metric> other = (Collection<Metric>)argument;
                Set<Metric> actual = new HashSet<>(other);

                if ( expected.size() != actual.size()) {
                    return false;
                }

                outer: for ( Metric exp : expected ) {
                    for ( Metric act : actual ) {
                        if ( exp.name().equals( act.name() )
                                && exp.value() == act.value()
                                && exp.timestamp() - exp.timestamp() % nextLevel.frequency() == act.timestamp() ) {
                            actual.remove( act );
                            continue outer;
                        }
                    }
                    return false;
                }
                return actual.size() == 0;
            } else {
                return false;
            }
        }

        @Override
        public void describeTo( Description desc ) {
            desc.appendText( "Expected metrics:\n\t" + StringUtils.join( expected, "\n\t" ) );
        }
    }


    private Metric[] metricsInSource = new Metric[] {
            new Metric( METRIC_NAME, 1410393795, 2.0074e+08 ),
            new Metric( METRIC_NAME, 1410394095, 2.0076e+08 ),
            new Metric( METRIC_NAME, 1410394395, 2.0078e+08 ),
            new Metric( METRIC_NAME, 1410394695, 2.0079e+08 ),
            new Metric( METRIC_NAME, 1410394995, 2.0081e+08 ),
            new Metric( METRIC_NAME, 1410395295, 2.0082e+08 ),
            new Metric( METRIC_NAME, 1410395595, 2.0085e+08 ),
            new Metric( METRIC_NAME, 1410395895, 2.0086e+08 ),
            new Metric( METRIC_NAME, 1410396195, 2.0088e+08 ),
            new Metric( METRIC_NAME, 1410396495, 2.0089e+08 ),
            new Metric( METRIC_NAME, 1410396795, 2.0091e+08 ),
            new Metric( METRIC_NAME, 1410397095, 2.0092e+08 ),
            new Metric( METRIC_NAME, 1410397395, 2.0095e+08 ),
            new Metric( METRIC_NAME, 1410397695, 2.0096e+08 ),
            new Metric( METRIC_NAME, 1410397995, 2.0098e+08 ),
            new Metric( METRIC_NAME, 1410398295, 2.0099e+08 ),
            new Metric( METRIC_NAME, 1410398595, 2.0102e+08 ),
            new Metric( METRIC_NAME, 1410398895, 2.0103e+08 ),
            new Metric( METRIC_NAME, 1410399195, 2.0105e+08 ),
            new Metric( METRIC_NAME, 1410399495, 2.0106e+08 ),
            new Metric( METRIC_NAME, 1410399795, 2.0108e+08 ),
            new Metric( METRIC_NAME, 1410400095, 2.011e+08 ),
            new Metric( METRIC_NAME, 1410400395, 2.0112e+08 ),
            new Metric( METRIC_NAME, 1410400695, 2.0113e+08 ),
            new Metric( METRIC_NAME, 1410400995, 2.0115e+08 ),
            new Metric( METRIC_NAME, 1410401295, 2.0117e+08 ),
            new Metric( METRIC_NAME, 1410401595, 2.0118e+08 ),
            new Metric( METRIC_NAME, 1410401895, 2.012e+08 ),
            new Metric( METRIC_NAME, 1410402195, 2.0122e+08 ),
            new Metric( METRIC_NAME, 1410402495, 2.0123e+08 ),
            new Metric( METRIC_NAME, 1410402795, 2.0126e+08 ),
            new Metric( METRIC_NAME, 1410403095, 2.0127e+08 ),
            new Metric( METRIC_NAME, 1410403395, 2.0129e+08 ),
            new Metric( METRIC_NAME, 1410403695, 2.013e+08 ),
            new Metric( METRIC_NAME, 1410403995, 2.0132e+08 ),
            new Metric( METRIC_NAME, 1410404295, 2.0133e+08 ),
            new Metric( METRIC_NAME, 1410404595, 2.0136e+08 ),
            new Metric( METRIC_NAME, 1410404895, 2.0137e+08 ),
            new Metric( METRIC_NAME, 1410405195, 2.0139e+08 ),
            new Metric( METRIC_NAME, 1410405495, 2.014e+08 ),
            new Metric( METRIC_NAME, 1410405795, 2.0143e+08 ),
            new Metric( METRIC_NAME, 1410406095, 2.0144e+08 ),
            new Metric( METRIC_NAME, 1410406395, 2.0146e+08 ),
            new Metric( METRIC_NAME, 1410406695, 2.0147e+08 ),
            new Metric( METRIC_NAME, 1410406995, 2.0149e+08 ),
            new Metric( METRIC_NAME, 1410407295, 2.0151e+08 ),
            new Metric( METRIC_NAME, 1410407595, 2.0153e+08 ),
            new Metric( METRIC_NAME, 1410407895, 2.0154e+08 ),
            new Metric( METRIC_NAME, 1410408195, 2.0156e+08 ),
            new Metric( METRIC_NAME, 1410408495, 2.0158e+08 ),
            new Metric( METRIC_NAME, 1410408795, 2.0159e+08 ),
            new Metric( METRIC_NAME, 1410409095, 2.0161e+08 ),
            new Metric( METRIC_NAME, 1410409395, 2.0163e+08 ),
            new Metric( METRIC_NAME, 1410409695, 2.0164e+08 ),
            new Metric( METRIC_NAME, 1410409995, 2.0167e+08 ),
            new Metric( METRIC_NAME, 1410410295, 2.0168e+08 ),
            new Metric( METRIC_NAME, 1410410595, 2.017e+08 ),
            new Metric( METRIC_NAME, 1410410895, 2.0171e+08 ),
            new Metric( METRIC_NAME, 1410411195, 2.0173e+08 ),
            new Metric( METRIC_NAME, 1410411495, 2.0174e+08 ),
            new Metric( METRIC_NAME, 1410411795, 2.0177e+08 ),
            new Metric( METRIC_NAME, 1410412095, 2.0179e+08 ),
            new Metric( METRIC_NAME, 1410412395, 2.018e+08 ),
            new Metric( METRIC_NAME, 1410412695, 2.0182e+08 ),
            new Metric( METRIC_NAME, 1410412995, 2.0184e+08 ),
            new Metric( METRIC_NAME, 1410413295, 2.0185e+08 ),
            new Metric( METRIC_NAME, 1410413595, 2.0187e+08 ),
            new Metric( METRIC_NAME, 1410413895, 2.0188e+08 ),
            new Metric( METRIC_NAME, 1410414195, 2.0191e+08 ),
            new Metric( METRIC_NAME, 1410414495, 2.0192e+08 ),
            new Metric( METRIC_NAME, 1410414795, 2.0194e+08 ),
            new Metric( METRIC_NAME, 1410415095, 2.0196e+08 ),
            new Metric( METRIC_NAME, 1410415395, 2.0198e+08 ),
            new Metric( METRIC_NAME, 1410415695, 2.0199e+08 ),
            new Metric( METRIC_NAME, 1410415995, 2.0202e+08 ),
            new Metric( METRIC_NAME, 1410416295, 2.0203e+08 ),
            new Metric( METRIC_NAME, 1410416595, 2.0205e+08 ),
            new Metric( METRIC_NAME, 1410416895, 2.0206e+08 ),
            new Metric( METRIC_NAME, 1410417195, 2.021e+08 ),
            new Metric( METRIC_NAME, 1410417495, 2.0212e+08 ),
            new Metric( METRIC_NAME, 1410417795, 2.0215e+08 ),
            new Metric( METRIC_NAME, 1410418095, 2.0217e+08 ),
            new Metric( METRIC_NAME, 1410418395, 2.0221e+08 ),
            new Metric( METRIC_NAME, 1410418695, 2.0224e+08 ),
            new Metric( METRIC_NAME, 1410418995, 2.0226e+08 ),
            new Metric( METRIC_NAME, 1410419295, 2.0229e+08 ),
            new Metric( METRIC_NAME, 1410419595, 2.0233e+08 ),
            new Metric( METRIC_NAME, 1410419895, 2.0236e+08 ),
            new Metric( METRIC_NAME, 1410420195, 2.0239e+08 ),
            new Metric( METRIC_NAME, 1410420495, 2.0242e+08 ),
            new Metric( METRIC_NAME, 1410420795, 2.0246e+08 ),
            new Metric( METRIC_NAME, 1410421095, 2.0248e+08 ),
            new Metric( METRIC_NAME, 1410421395, 2.0252e+08 ),
            new Metric( METRIC_NAME, 1410421695, 2.0254e+08 ),
            new Metric( METRIC_NAME, 1410421995, 2.0258e+08 ),
            new Metric( METRIC_NAME, 1410422295, 2.0261e+08 ),
            new Metric( METRIC_NAME, 1410422595, 2.0264e+08 ),
            new Metric( METRIC_NAME, 1410422895, 2.0267e+08 ),
            new Metric( METRIC_NAME, 1410423195, 2.0271e+08 ),
            new Metric( METRIC_NAME, 1410423495, 2.0274e+08 ),
            new Metric( METRIC_NAME, 1410423795, 2.0278e+08 ),
            new Metric( METRIC_NAME, 1410424095, 2.0281e+08 ),
            new Metric( METRIC_NAME, 1410424395, 2.0285e+08 ),
            new Metric( METRIC_NAME, 1410424695, 2.0288e+08 ),
            new Metric( METRIC_NAME, 1410424995, 2.0291e+08 ),
            new Metric( METRIC_NAME, 1410425295, 2.0293e+08 ),
            new Metric( METRIC_NAME, 1410425595, 2.0298e+08 ),
            new Metric( METRIC_NAME, 1410425895, 2.03e+08 ),
            new Metric( METRIC_NAME, 1410426195, 2.0304e+08 ),
            new Metric( METRIC_NAME, 1410426495, 2.0307e+08 ),
            new Metric( METRIC_NAME, 1410426795, 2.031e+08 ),
            new Metric( METRIC_NAME, 1410427095, 2.0313e+08 ),
            new Metric( METRIC_NAME, 1410427395, 2.0317e+08 ),
            new Metric( METRIC_NAME, 1410427695, 2.0319e+08 ),
            new Metric( METRIC_NAME, 1410427995, 2.0323e+08 ),
            new Metric( METRIC_NAME, 1410428295, 2.0325e+08 ),
            new Metric( METRIC_NAME, 1410428595, 2.0329e+08 ),
            new Metric( METRIC_NAME, 1410428895, 2.0331e+08 ),
            new Metric( METRIC_NAME, 1410429195, 2.0335e+08 ),
            new Metric( METRIC_NAME, 1410429495, 2.0338e+08 ),
            new Metric( METRIC_NAME, 1410429795, 2.0341e+08 ),
            new Metric( METRIC_NAME, 1410430095, 2.0343e+08 ),
            new Metric( METRIC_NAME, 1410430395, 2.0346e+08 ),
            new Metric( METRIC_NAME, 1410430695, 2.0348e+08 ),
            new Metric( METRIC_NAME, 1410430995, 2.0351e+08 ),
            new Metric( METRIC_NAME, 1410431295, 2.0353e+08 ),
            new Metric( METRIC_NAME, 1410431595, 2.0356e+08 ),
            new Metric( METRIC_NAME, 1410431895, 2.0358e+08 ),
            new Metric( METRIC_NAME, 1410432195, 2.0361e+08 ),
            new Metric( METRIC_NAME, 1410432495, 2.0363e+08 ),
            new Metric( METRIC_NAME, 1410432795, 2.0365e+08 ),
            new Metric( METRIC_NAME, 1410433095, 2.0367e+08 ),
            new Metric( METRIC_NAME, 1410433395, 2.037e+08 ),
            new Metric( METRIC_NAME, 1410433695, 2.0373e+08 ),
            new Metric( METRIC_NAME, 1410433995, 2.0375e+08 ),
            new Metric( METRIC_NAME, 1410434295, 2.0377e+08 ),
            new Metric( METRIC_NAME, 1410434595, 2.0381e+08 ),
            new Metric( METRIC_NAME, 1410434895, 2.0383e+08 ),
            new Metric( METRIC_NAME, 1410435195, 2.0386e+08 ),
            new Metric( METRIC_NAME, 1410435495, 2.0388e+08 ),
            new Metric( METRIC_NAME, 1410435795, 2.0391e+08 ),
            new Metric( METRIC_NAME, 1410436095, 2.0393e+08 ),
            new Metric( METRIC_NAME, 1410436395, 2.0396e+08 ),
            new Metric( METRIC_NAME, 1410436695, 2.0398e+08 ),
            new Metric( METRIC_NAME, 1410436995, 2.0401e+08 ),
            new Metric( METRIC_NAME, 1410437295, 2.0403e+08 ),
            new Metric( METRIC_NAME, 1410437595, 2.0406e+08 ),
            new Metric( METRIC_NAME, 1410437895, 2.0408e+08 ),
            new Metric( METRIC_NAME, 1410438195, 2.0411e+08 ),
            new Metric( METRIC_NAME, 1410438495, 2.0413e+08 ),
            new Metric( METRIC_NAME, 1410438795, 2.0416e+08 ),
            new Metric( METRIC_NAME, 1410439095, 2.0421e+08 ),
            new Metric( METRIC_NAME, 1410439395, 2.0426e+08 ),
            new Metric( METRIC_NAME, 1410439695, 2.0429e+08 ),
            new Metric( METRIC_NAME, 1410439995, 2.0433e+08 ),
            new Metric( METRIC_NAME, 1410440295, 2.0436e+08 ),
            new Metric( METRIC_NAME, 1410440595, 2.0439e+08 ),
            new Metric( METRIC_NAME, 1410440895, 2.0443e+08 ),
            new Metric( METRIC_NAME, 1410441195, 2.0447e+08 ),
            new Metric( METRIC_NAME, 1410441495, 2.045e+08 ),
            new Metric( METRIC_NAME, 1410441795, 2.0454e+08 ),
            new Metric( METRIC_NAME, 1410442095, 2.0458e+08 ),
            new Metric( METRIC_NAME, 1410442395, 2.0462e+08 ),
            new Metric( METRIC_NAME, 1410442695, 2.0466e+08 ),
            new Metric( METRIC_NAME, 1410442995, 2.0469e+08 ),
            new Metric( METRIC_NAME, 1410443295, 2.0473e+08 ),
            new Metric( METRIC_NAME, 1410443595, 2.0476e+08 ),
            new Metric( METRIC_NAME, 1410443895, 2.0479e+08 ),
            new Metric( METRIC_NAME, 1410444195, 2.0482e+08 ),
            new Metric( METRIC_NAME, 1410444495, 2.0486e+08 ),
            new Metric( METRIC_NAME, 1410444795, 2.0489e+08 ),
            new Metric( METRIC_NAME, 1410445095, 2.0492e+08 ),
            new Metric( METRIC_NAME, 1410445395, 2.0496e+08 ),
            new Metric( METRIC_NAME, 1410445695, 2.0499e+08 ),
            new Metric( METRIC_NAME, 1410445995, 2.0502e+08 ),
            new Metric( METRIC_NAME, 1410446295, 2.0505e+08 ),
            new Metric( METRIC_NAME, 1410446595, 2.0508e+08 ),
            new Metric( METRIC_NAME, 1410446895, 2.051e+08 ),
            new Metric( METRIC_NAME, 1410447195, 2.0513e+08 ),
            new Metric( METRIC_NAME, 1410447495, 2.0515e+08 ),
            new Metric( METRIC_NAME, 1410447795, 2.0517e+08 ),
            new Metric( METRIC_NAME, 1410448095, 2.0519e+08 ),
            new Metric( METRIC_NAME, 1410448395, 2.0522e+08 ),
            new Metric( METRIC_NAME, 1410448695, 2.0523e+08 ),
            new Metric( METRIC_NAME, 1410448995, 2.0526e+08 ),
            new Metric( METRIC_NAME, 1410449295, 2.0528e+08 ),
            new Metric( METRIC_NAME, 1410449595, 2.053e+08 ),
            new Metric( METRIC_NAME, 1410449895, 2.0531e+08 ),
            new Metric( METRIC_NAME, 1410450195, 2.0533e+08 ),
            new Metric( METRIC_NAME, 1410450495, 2.0534e+08 ),
            new Metric( METRIC_NAME, 1410450795, 2.0536e+08 ),
            new Metric( METRIC_NAME, 1410451095, 2.0537e+08 ),
            new Metric( METRIC_NAME, 1410451395, 2.0539e+08 ),
            new Metric( METRIC_NAME, 1410451695, 2.0541e+08 ),
            new Metric( METRIC_NAME, 1410451995, 2.0543e+08 ),
            new Metric( METRIC_NAME, 1410452295, 2.0543e+08 ),
            new Metric( METRIC_NAME, 1410452595, 2.0546e+08 ),
            new Metric( METRIC_NAME, 1410452895, 2.0546e+08 ),
            new Metric( METRIC_NAME, 1410453195, 2.0548e+08 ),
            new Metric( METRIC_NAME, 1410453495, 2.0549e+08 ),
            new Metric( METRIC_NAME, 1410453795, 2.0552e+08 ),
            new Metric( METRIC_NAME, 1410454095, 2.0553e+08 ),
            new Metric( METRIC_NAME, 1410454395, 2.0554e+08 ),
            new Metric( METRIC_NAME, 1410454695, 2.0555e+08 ),
            new Metric( METRIC_NAME, 1410454995, 2.0557e+08 ),
            new Metric( METRIC_NAME, 1410455295, 2.0559e+08 ),
            new Metric( METRIC_NAME, 1410455595, 2.0561e+08 ),
            new Metric( METRIC_NAME, 1410455895, 2.0562e+08 ),
            new Metric( METRIC_NAME, 1410456195, 2.0564e+08 ),
            new Metric( METRIC_NAME, 1410456495, 2.0565e+08 ),
            new Metric( METRIC_NAME, 1410456795, 2.0567e+08 ),
            new Metric( METRIC_NAME, 1410457095, 2.0568e+08 ),
            new Metric( METRIC_NAME, 1410457395, 2.057e+08 ),
            new Metric( METRIC_NAME, 1410457695, 2.0571e+08 ),
            new Metric( METRIC_NAME, 1410457995, 2.0573e+08 ),
            new Metric( METRIC_NAME, 1410458295, 2.0574e+08 ),
            new Metric( METRIC_NAME, 1410458595, 2.0575e+08 ),
            new Metric( METRIC_NAME, 1410458895, 2.0576e+08 ),
            new Metric( METRIC_NAME, 1410459195, 2.0579e+08 ),
            new Metric( METRIC_NAME, 1410459495, 2.0579e+08 ),
            new Metric( METRIC_NAME, 1410459795, 2.0581e+08 ),
            new Metric( METRIC_NAME, 1410460095, 2.0582e+08 ),
            new Metric( METRIC_NAME, 1410460395, 2.0584e+08 ),
            new Metric( METRIC_NAME, 1410460695, 2.0585e+08 ),
            new Metric( METRIC_NAME, 1410460995, 2.0587e+08 ),
            new Metric( METRIC_NAME, 1410461295, 2.0588e+08 ),
            new Metric( METRIC_NAME, 1410461595, 2.059e+08 ),
            new Metric( METRIC_NAME, 1410461895, 2.0591e+08 ),
            new Metric( METRIC_NAME, 1410462195, 2.0593e+08 ),
            new Metric( METRIC_NAME, 1410462495, 2.0594e+08 ),
            new Metric( METRIC_NAME, 1410462795, 2.0596e+08 ),
            new Metric( METRIC_NAME, 1410463095, 2.0597e+08 ),
            new Metric( METRIC_NAME, 1410463395, 2.0599e+08 ),
            new Metric( METRIC_NAME, 1410463695, 2.06e+08 ),
            new Metric( METRIC_NAME, 1410463995, 2.0602e+08 ),
            new Metric( METRIC_NAME, 1410464295, 2.0603e+08 ),
            new Metric( METRIC_NAME, 1410464595, 2.0605e+08 ),
            new Metric( METRIC_NAME, 1410464895, 2.0606e+08 ),
            new Metric( METRIC_NAME, 1410465195, 2.0608e+08 ),
            new Metric( METRIC_NAME, 1410465495, 2.0609e+08 ),
            new Metric( METRIC_NAME, 1410465795, 2.0611e+08 ),
            new Metric( METRIC_NAME, 1410466095, 2.0613e+08 ),
            new Metric( METRIC_NAME, 1410466395, 2.0615e+08 ),
            new Metric( METRIC_NAME, 1410466695, 2.0615e+08 ),
            new Metric( METRIC_NAME, 1410466995, 2.0617e+08 ),
            new Metric( METRIC_NAME, 1410467295, 2.0618e+08 ),
            new Metric( METRIC_NAME, 1410467595, 2.062e+08 ),
            new Metric( METRIC_NAME, 1410467895, 2.0621e+08 ),
            new Metric( METRIC_NAME, 1410468195, 2.0624e+08 ),
            new Metric( METRIC_NAME, 1410468495, 2.0624e+08 ),
            new Metric( METRIC_NAME, 1410468795, 2.0627e+08 ),
            new Metric( METRIC_NAME, 1410469095, 2.0627e+08 ),
            new Metric( METRIC_NAME, 1410469395, 2.0629e+08 ),
            new Metric( METRIC_NAME, 1410469695, 2.063e+08 ),
            new Metric( METRIC_NAME, 1410469995, 2.0632e+08 ),
            new Metric( METRIC_NAME, 1410470295, 2.0633e+08 ),
            new Metric( METRIC_NAME, 1410470595, 2.0635e+08 ),
            new Metric( METRIC_NAME, 1410470895, 2.0636e+08 ),
            new Metric( METRIC_NAME, 1410471195, 2.0638e+08 ),
            new Metric( METRIC_NAME, 1410471495, 2.0639e+08 ),
            new Metric( METRIC_NAME, 1410471795, 2.0641e+08 ),
            new Metric( METRIC_NAME, 1410472095, 2.0642e+08 ),
            new Metric( METRIC_NAME, 1410472395, 2.0644e+08 ),
            new Metric( METRIC_NAME, 1410472695, 2.0645e+08 ),
            new Metric( METRIC_NAME, 1410472995, 2.0646e+08 ),
            new Metric( METRIC_NAME, 1410473295, 2.0647e+08 ),
            new Metric( METRIC_NAME, 1410473595, 2.0649e+08 ),
            new Metric( METRIC_NAME, 1410473895, 2.065e+08 ),
            new Metric( METRIC_NAME, 1410474195, 2.0652e+08 ),
            new Metric( METRIC_NAME, 1410474495, 2.0653e+08 ),
            new Metric( METRIC_NAME, 1410474795, 2.0655e+08 ),
            new Metric( METRIC_NAME, 1410475095, 2.0656e+08 ),
            new Metric( METRIC_NAME, 1410475395, 2.0658e+08 ),
            new Metric( METRIC_NAME, 1410475695, 2.0659e+08 ),
            new Metric( METRIC_NAME, 1410475995, 2.0661e+08 ),
            new Metric( METRIC_NAME, 1410476295, 2.0662e+08 ),
            new Metric( METRIC_NAME, 1410476595, 2.0663e+08 ),
            new Metric( METRIC_NAME, 1410476895, 2.0664e+08 ),
            new Metric( METRIC_NAME, 1410477195, 2.0666e+08 ),
            new Metric( METRIC_NAME, 1410477495, 2.0667e+08 ),
            new Metric( METRIC_NAME, 1410477795, 2.0669e+08 ),
            new Metric( METRIC_NAME, 1410478095, 2.067e+08 ),
            new Metric( METRIC_NAME, 1410478395, 2.0672e+08 ),
            new Metric( METRIC_NAME, 1410478695, 2.0673e+08 ),
            new Metric( METRIC_NAME, 1410478995, 2.0675e+08 ),
            new Metric( METRIC_NAME, 1410479295, 2.0676e+08 ),
            new Metric( METRIC_NAME, 1410479595, 2.0678e+08 ),
            new Metric( METRIC_NAME, 1410479895, 2.0679e+08 )
    };
}

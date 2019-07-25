package eu.profinit.manta.connector.java.analysis.kafka;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.AbstractAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaConsumerConfigHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaConsumerHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaConsumerRecordHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaProducerConfigHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaProducerHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaProducerRecordHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaTopicPartitionHandler;
import eu.profinit.manta.connector.java.analysis.kafka.handler.PropertiesHandler;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_RECORD;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_RECORD;

/**
 * Plugin for Kafka Framework
 */
public class KafkaAnalysisPlugin extends AbstractAnalysisPlugin {
    @Nonnull
    @Override
    protected CallHandlers getCallHandlers(@Nonnull final ClassMethodCache classMethodCache) {
        return new CallHandlers(this,
                new KafkaProducerHandler(classMethodCache),
                new KafkaProducerConfigHandler(classMethodCache),
                new KafkaProducerRecordHandler(classMethodCache),
                new KafkaConsumerHandler(classMethodCache),
                new KafkaConsumerConfigHandler(classMethodCache),
                new KafkaConsumerRecordHandler(classMethodCache),
                new KafkaTopicPartitionHandler(classMethodCache),
                new PropertiesHandler());
    }

    @Nonnull
    @Override
    protected List<IMethod> getMethodsToAnalyse() throws NoSuchMethodException {
        final List<IMethod> result = Stream.of(
                WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, CONSUMER).stream()
                        .filter(new ConstructorWithConfigurationPredicate()),
                WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, CONSUMER_RECORD).stream()
                        .filter(IMethod::isInit),
                WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, PRODUCER).stream()
                        .filter(new ConstructorWithConfigurationPredicate()),
                WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, PRODUCER_RECORD).stream()
                        .filter(IMethod::isInit))
                .flatMap(stream -> stream)
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            return result;
        }
        throw new NoSuchMethodException("Kafka classes were not found!");
    }

    private static class ConstructorWithConfigurationPredicate implements Predicate<IMethod> {
        private static final List<ClassWrapper> CONFIGURATION_CLASSES = ImmutableList.of(new ClassWrapperImpl(Map.class),
                new ClassWrapperImpl(Properties.class));

        @Override
        public boolean test(IMethod iMethod) {
            if (!iMethod.isInit()) {
                return false;
            }
            final List<ClassWrapper> arguments = WalaUtils.getArguments(iMethod);
            if (arguments.size() < 1) {
                return false;
            }
            return CONFIGURATION_CLASSES.stream().anyMatch(clazz -> ClassWrapper.isSame(arguments.get(0), clazz));
        }
    }
}

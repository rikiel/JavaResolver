package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdTestModel;

public class MyBatisComplexFeaturesXmlMappers {
    public interface SelectAllIncludedXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAll();
    }

    public interface SelectAllIfXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllLike(String value);
    }

    public interface SelectAllWithCommentsXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAll();
    }

    public interface SelectAllForeachXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllForeachDefaultValuesXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllChooseXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllWhereXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllWhereTrimXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface UpdateWithSetXmlMapper {
        void updateAll(MyBatisSingleIdTestModel model);
    }

    public interface UpdateWithSetTrimXmlMapper {
        void updateAll(MyBatisSingleIdTestModel model);
    }

    public interface UpdateValuesFromMapMapper {
        void update(MyBatisSingleIdTestModel model, Map<String, String> valueMap);
    }

    /**
     * Uses some values in map in select and then update map with result
     */
    public interface SelectToMapMapper {
        void selectForMap(Map<String, Object> values);
    }

    public interface SelectToListMapper {
        void selectToList(ListHolder resultValues);

        class ListHolder {
            private List<MyBatisSingleIdTestModel> result;

            public List<MyBatisSingleIdTestModel> getResult() {
                return result;
            }

            public void setList(List<MyBatisSingleIdTestModel> result) {
                this.result = result;
            }
        }
    }
}

package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid;

public class MyBatisSingleIdTestModel {
    private Integer id;
    private String value;

    public MyBatisSingleIdTestModel() {
    }

    public MyBatisSingleIdTestModel(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

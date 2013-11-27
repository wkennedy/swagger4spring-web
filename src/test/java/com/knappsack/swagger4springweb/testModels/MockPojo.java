package com.knappsack.swagger4springweb.testModels;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "MockPojo", description = "A basic pojo for testing API documentation")
public class MockPojo {

    @ApiModelProperty(value = "id", dataType = "long")
    private long id;
    @ApiModelProperty(value = "name", dataType = "String")
    private String name;
    @ApiModelProperty(value = "description", dataType = "String")
    private String description;

    private Collection<MockPojoChild> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<MockPojoChild> getChildren() {
        return children;
    }

    public void setChildren(List<MockPojoChild> children) {
        this.children = children;
    }
}

package com.knappsack.swagger4springweb.testModels;

import java.util.Collection;
import java.util.List;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiClass(value = "MockPojo", description = "A basic pojo for testing API documentation")
public class MockPojo {

    @ApiProperty(value = "id", dataType = "long")
    private long id;
    @ApiProperty(value = "name", dataType = "String")
    private String name;
    @ApiProperty(value = "description", dataType = "String")
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

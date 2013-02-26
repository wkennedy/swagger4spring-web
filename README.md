swagger4spring-web
==================

This project aims at providing Swagger support to your Spring-Web based application.  It will attempt to document your API based on existing Spring-Web annotations if no Swagger annotations exist.  If Swagger annotations do exist, it will utilize those in conjunction with the Spring-Web annotations.

##How-To

To include swagger4spring-web in your project, you need to include the jar in your project.  If you use Maven, please include the following dependency:

        <dependency>
            <groupId>com.knappsack</groupId>
            <artifactId>swagger4spring-web</artifactId>
            <version>0.1.2</version>
        </dependency>


In order to use swagger4spring-web in your project, you need to declare an ApiDocumentationController bean in your
servlet context.  For example:

        <beans:bean id="documentationController" class="com.knappsack.swagger4springweb.controller.ApiDocumentationController"
                p:basePath="http://localhost:8080/yourapp"
                p:baseControllerPackage="com.knappsack.swagger4springweb.controllers.api"
                p:baseModelPackage="com.knappsack.swagger4springweb.models"
                p:apiVersion="v1" />

* basePath - optional - the base URL of your web application, for example http://localhost/swagger4spring-web-example
* baseControllerPackage - optional - this is the package you want swagger4spring-web to scan to look for classes annotated with @Controller.  If this is not set, all your packages are scanned.
* baseModelPackage - optional - this is the package you want to scan if all your model objects are in a specific directory.  These classes will be added to your documentation schema.  If no package is specified only certain return types and parameters are added to the documentation schema.
* apiVersion - required - this is the version of your API

Once the ApiDocumentionController is wired, you may call go to your base path + /api/resourceList (ex: http://localhost/swagger4spring-web-example/api/resourceList) in order to retrieve an inventory of your APIs.
For an example JSP see this [page](https://github.com/wkennedy/swagger4spring-web-example/blob/master/src/main/webapp/WEB-INF/views/documentation.jsp)

To see a working example, please take a look at [swagger4spring-web-example](https://github.com/wkennedy/swagger4spring-web-example/ "swagger4spring-web-example").

##Annotation Support
The following Spring-Web annotations are supported:

* @Controller
* @RequestMapping
* @ResponseBody
* @RequestBody
* @PathVariable
* @RequestParam

The following Swagger annotations are supported:

* @Api
* @ApiError
* @ApiErrors
* @ApiOperation
* @ApiParam

##External Links
[Swagger Home](http://developers.helloreverb.com/swagger/ "Swagger Home")

[Swagger Wiki](https://github.com/wordnik/swagger-core/wiki "Swagger Wiki")


##License
Copyright (c) 2013 Will Kennedy

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

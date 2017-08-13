=====================================================
== HOW TO CONFIGURE SWAGGER INTO JOOMLA CONTENT ==
=====================================================

Author      : Guilherme Viteri (gviteri@ebi.ac.uk)
Create date : 14-08-2017
Team        : Reactome

1. Introduction

    Adding Swagger API Documentation into WordPress content and fix conflicts issues.

2. Mandatory Changes in swagger files

    2.1 swagger-ui.js

        1. Class submit takes the style from WordPress and makes the button larger.
            - Search for value='Try it out'
            - Replace class="submit" to class="swagger-submit"

        2. Replicate .submit throughout the file
            - Search for 'click .swagger-submit'
            - Replace .submit to .swagger-submit

3. index.jsp

    2. onComplete swagger callback function

        - Swagger footer collides to WordPress style
            $(".swagger-section .footer").attr("class", "swagger-footer");
            $('.swagger-footer').css('padding-top', '25px');

        - Apply WordPress font style in its header and footer
        $('div.footer').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');
        $('div.navwrapper').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');

4. additional.jsp
    Once updated swagger lib, js, images and css, please ensure the path is pointing to "swagger/"
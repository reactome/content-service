=====================================================
== HOW TO CONFIGURE SWAGGER INTO WORDPRESS CONTENT ==
=====================================================

Author      : Guilherme Viteri (gviteri@ebi.ac.uk)
Create date : 18-03-2016
Team        : Reactome

1. Introduction

    Adding Swagger API Documentation into WordPress content and fix conflicts issues.

2. Mandatory Changes in swagger files

    2.1 reset.css

        - Add '.reset-swagger' class in front of all attributes in order to reset styles in the tags inside it.
        - Leave html and body without .reset-swagger

        e.g.
            html,
            body,
            .reset-swagger div,
            .reset-swagger span,
            .reset-swagger h1,
            .reset-swagger h2,

    2.2 swagger-ui.js

        1. Class submit takes the style from WordPress and makes the button larger.
            - Search for value='Try it out'
            - Replace class="submit" to class="swagger-submit"

        2. Replicate .submit throughout the file
            - Search for 'click .swagger-submit'
            - Replace .submit to .swagger-submit

    2.3 swagger-ui-min.js

        1. If you are using the minified js file then follow the same steps mentioned in the section 2.2


3. index.jsp

    1. The swagger div content must be wrapped by reset-swagger div
        <div class="reset-swagger"> <-- Reset will be applied inside this div only
            <div class="swagger-section">
                <!-- Swagger places the documentation into container id swagger-ui-container -->
                <div id="swagger-ui-container" class="swagger-ui-wrap"></div>
            </div>
        </div>

    2. onComplete swagger callback function

        - Swagger footer collides to WordPress style
            $(".swagger-section .footer").attr("class", "swagger-footer");
            $('.swagger-footer').css('padding-top', '25px');

        - Apply WordPress font style in its header and footer
        $('div.footer').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');
        $('div.navwrapper').css('font', '13px/1.5 "Helvetica Neue", Arial, "Liberation Sans", FreeSans, sans-serif');


4. scripts.jsp
    Once updated swagger lib, js, images and css, please ensure the path is pointing to "swagger/"
# SlideshowFX developer documentation

## Introduction

SlideshowFX is an application allowing you to create HTML5 based presentation in a very common manner. Nowadays we all use PowerPoint, Keynote and their friends but with evolution of web technologies, new perspectives are now possible. Indeed there is a lot of HTML5/JavaScript based framework, like [reveal.js](http://lab.hakim.se/reveal-js/) or [impress.js](https://github.com/bartaz/impress.js/), that allow you to create beautiful presentations to keep your audience active. It brings you a lot of freedom like adding a YouTube video right inside your presentation, playing music, automatically syntax highlighting code example and more. But to do such presentations you have to know HTML, CSS and JavaScript programming languages.

SlideshowFX lets programming languages to developers and presentations' creation to end users. This means that you will be able to create your presentations without knowing HTML, even if it is recommended to have the basics.

But SlideshowFX isn't just another simple presentation engine. It brings much more for you:

- A chat is provided so your audience can send you questions using their smartphones and you will answer them whenever you want ;
- A quiz creator so your audience can answer your quiz using their smartphones and you see results live ;
- Save your presentations on Box, Dropbox or Google Drive ;
- Insert code snippets that can be executed directly from the presentation and console's output is displayed in the slide.

## Developer guide

### Tools & APIs

| API, Tool | Version | Comments |
|-----------|---------|----------|
| OpenJDK | ${jdk_version} | |
| OpenJFX | ${javafx_version} | |
| asciidoctorj | ${asciidoctorj_version} | Used by the `slideshowfx-asciidoctor` plugin |
| [Atlassian CommonMark](https://github.com/atlassian/commonmark-java) | ${commonmark_version} | Used by the `slideshowfx-markdown` plugin |
| [FontAwesome](https://fontawesome.com/) | ${fontawesome_version} | |
| freemarker | ${freemarker_version} | |
| jsoup | ${jsoup_version} | |
| org.eclipse.mylyn.wikitext.textile.core | ${wikitext_textile_core_version} | Used by the `slideshowfx-textile` plugin |
| [Snap.svg](http://snapsvg.io/) | ${snapsvg_version} | Used by the `slideshowfx-shape-extension` plugin |
| [SweetAlert2](https://sweetalert2.github.io/) | ${sweetalert_version} | Used by the `slideshowfx-alert-extension` plugin |
| vertx-core | ${vertx_version} | |
| zxing-jse | ${zxing_jse_version} | |
| [ACE](http://ace.c9.io/) | ${ace_version} | |

### Project structure

SlideshowFX contains the following modules:

| Module | Description |
|--------|-------------|
| `slideshowfx-app` | Module for the desktop client of SlideshowFX |
| `slideshowfx-documentation` | Documentation of the application |
| `slideshowfx-utils` | Utilities classes that may be used in other modules |
| `slideshowfx-engines` | Engines used in SlideshowFX for working with templates and presentations |
| `slideshowfx-controls` | Custom graphical controls to be used in other modules |
| `slideshowfx-global-configuration` | Accesses and interactions with the SlideshowFX configuration |
| `slideshowfx-logs` | Logging implementation |
| `slideshowfx-plugin-manager` | Plugin mechanism |
| `slideshowfx-server` | Internal server used by SlideshowFX |
| `slideshowfx-markup` | Contract's definition to create a plugin for supporting a new markup syntax in SlideshowFX to define slides' content |
| `slideshowfx-asciidoctor` | Implementation of `slideshowfx-markup` to define slides' content in asciidoctor |
| `slideshowfx-html` | Implementation of `slideshowfx-markup` to define slides' content in HTML |
| `slideshowfx-markdown` | Implementation of `slideshowfx-markup` to define slides' content in markdown |
| `slideshowfx-textile` | Implementation of `slideshowfx-markup` to define slides' content in textile |
| `slideshowfx-content-extension` | Contract's definition to create a plugin module for inserting specific content in slides (like images, ...) |
| `slideshowfx-alert-extension` | Implementation of `slideshowfx-content-extension` to insert beautiful alerts in slides |
| `slideshowfx-code-extension` | Implementation of `slideshowfx-content-extension` to insert code in slides |
| `slideshowfx-image-extension` | Implementation of `slideshowfx-content-extension` to insert images in slides |
| `slideshowfx-link-extension` | Implementation of `slideshowfx-content-extension` to insert HTML links in slides |
| `slideshowfx-quiz-extension` | Implementation of `slideshowfx-content-extension` to insert quiz in slides |
| `slideshowfx-quote-extension` | Implementation of `slideshowfx-content-extension` to insert quotes in slides |
| `slideshowfx-sequence-diagram-extension` | Implementation of `slideshowfx-content-extension` to insert sequence diagrams in slides |
| `slideshowfx-shape-extension` | Implementation of `slideshowfx-content-extension` to insert shapes in slides |
| `slideshowfx-snippet-extension` | Implementation of `slideshowfx-content-extension` to insert executable code snippet in slides |
| `slideshowfx-hosting-connector` | Contract's definition to create a plugin for connecting to a file hosting service |
| `slideshowfx-box-hosting-connector` | Implementation of `slideshowfx-hosting-connector` to connect to Box |
| `slideshowfx-dropbox-hosting-connector` | Implementation of `slideshowfx-hosting-connector` to connect to Dropbox |
| `slideshowfx-drive-hosting-connector` | Implementation of `slideshowfx-hosting-connector` to connect to Google Drive |
| `slideshowfx-snippet-executor` | Contract's definition to create a plugin module for executing code snippet in a presentation |
| `slideshowfx-go-executor` | Implementation of `slideshowfx-snippet-executor` for executing Go code snippet in a presentation |
| `slideshowfx-golo-executor` | Implementation of `slideshowfx-snippet-executor` for executing Golo code snippet in a presentation |
| `slideshowfx-groovy-executor` | Implementation of `slideshowfx-snippet-executor` for executing Groovy code snippet in a presentation |
| `slideshowfx-java-executor` | Implementation of `slideshowfx-snippet-executor` for executing Java code snippet in a presentation |
| `slideshowfx-javascript-executor` | Implementation of `slideshowfx-snippet-executor` for executing JavaScript code snippet in a presentation |
| `slideshowfx-kotlin-executor` | Implementation of `slideshowfx-snippet-executor` for executing Kotlin code snippet in a presentation |
| `slideshowfx-ruby-executor` | Implementation of `slideshowfx-snippet-executor` for executing Ruby code snippet in a presentation |
| `slideshowfx-rust-executor` | Implementation of `slideshowfx-snippet-executor` for executing Rust code snippet in a presentation |
| `slideshowfx-scala-executor` | Implementation of `slideshowfx-snippet-executor` for executing Scala code snippet in a presentation |

### Gradle

SlideshowFX uses [gradle](https://gradle.org/) as build system. The version used is `${gradle_version}`.

#### Particularities

##### Documentation

The documentation provided within the SlideshowFX application is the same as the one provided by the setup. In order to avoid duplicate files, the `slideshowfx-app` module unpacks the `slideshowfx-documentation` HTML documentation in its own sources. In order to update the documentation within the application and display it in the application during development, the following command could be used:

```bash
gradlew :slideshowfx-app:processResources
```

##### Annotation processor

In order to ease the development of plugins, the `PluginProcessor` has been developed to automatically generate the services files for each plugin. The annotation processor takes classes annotated with the `com.twasyl.slideshowfx.plugin.Plugin` annotation.

#### Custom plugins

Custom gradle plugins have been developed for the project:

| Plugin | ID | Description |
|--------|----|-------------|
| Documentation | `documentation` | Render Markdown documentations to HTML. |
| Gherkin | `gherkin` | To be applied on projects using Gherkin tests. |
| SlideshowFXRelease | `sfx-release` | To be applied on root project for preparing the release |
| SlideshowFXPlugin | `sfx-plugin` | To be applied for projects representing a SlideshowFX plugin. |
| SlideshowFXPackager | `sfx-packager` | To be applied on projects that are JavaFX application and that needs to be packaged as an application. |

##### Documentation

Gradle plugin to render Markdown documentation to HTML.

###### Tasks

| Name | Description |
|------|-------------|
| `expandDocumentation` | Expands the Markdown documentation by replacing variables with their values |
| `renderDocumentation` | Renders the expanded documentation to HTML |

###### Extensions

| Name | Description |
|------|-------------|
| `documentation` | Allows to change the default behaviour of the plugin, like the source, css, js, expanded and render directories |

##### Gherkin

Gradle plugin to be applied on projects using Gherkin tests.

###### Tasks

| Name | Description |
|------|-------------|
| `gherkinTest` | Runs the Gherkin tests using Cucumber |

###### Configurations

| Name | Description |
|------|-------------|
| `gherkinTestImplementation` | Contains the required dependencies to write the Gherkin tests |

##### SlideshowFXRelease

Gradle plugin prepare a release.

###### Tasks

| Name | Description |
|------|-------------|
| `updateProductVersionNumber` | Update the product version number in all relevant files |
| `removeSnapshots` | Remove the -SNAPSHOT qualifier from versions from build scripts |

###### Extensions

| Name | Description |
|------|-------------|
| `release` | Allows to change the default behaviour of the plugin, like the next version token and the product version |

##### SlideshowFXPlugin

Gradle plugin to be applied for projects representing a SlideshowFX plugin.

###### Tasks

| Name | Description |
|------|-------------|
| `prepareManifest` |  Adds to the MANIFEST.MF file the attribute regarding the plugin manager. These attributes are defined by a `sfxPlugin { bundle { ... }}}` extension |
| `bundle` | Creating the bundle of the plugin (having the `.sfx-plugin` extension) |
| `installPlugin` | Installs the current plugin in the `\$user.home/.SlideshowFX/plugins` directory |
| `uninstallPlugin` | Uninstalls the **current** version of the plugin from the installation directory |
| `uninstallAllVersionsPlugin` | Uninstalls all versions of the plugin from the installation directory |

###### Configurations

| Name | Description |
|------|-------------|
| `bundles` | Contains the produced bundle of the plugin, typically the `.sfx-plugin` file |
| `pluginDependencies` | Used to define the dependencies required by the plugin. Dependencies provided in this configuration will be packaged within the plugin bundle |

###### Extensions

| Name | Description |
|------|-------------|
| `sfxPlugin` | Allows to define the plugin information |
| `bundle` | _(To be used in the `sfxPlugin` extension)_ Allows to define the bundle information |

##### SlideshowFXPackager

Gradle plugin to be applied on projects that are JavaFX application and that needs to be packaged as an application.

###### Tasks

| Name | Description |
|------|-------------|
| `prepareResources` |  Copies dependencies and additional resources in dedicated folders to be then packages |
| `createRuntime` | Creates a custom JDK image to be used as runtime for the targeted application |
| `createPackage` | Creates the native package of the application |

###### Extensions

| Name | Description |
|------|-------------|
| `packaging` | Allows to define the package configuration |

#### Custom tasks

Some gradle tasks are provided to help the developer:

| Task name | Description |
|-----------|-------------|
| `ideaCleanOutput` | removes all files produced by the IntelliJ compilation |
| `removeSnapshots` | removes the `-SNAPSHOT` qualifier from projects' versions |
| `updateProductVersionNumber` | updates the product version in source files |
| `:slideshowfx-app:createSlideContentEditor` | allows to build the `sfx-slide-content-editor.zip` archive |
| `:slideshowfx-alert-extension:createSweetAlertPackage` | allows to build the `sweetalert.zip` archive |
| `:slideshowfx-icons:updateFontAwesome` | allows to update the FontAwesome version shipped with SlideshowFX |
| `:slideshowfx-shape-extension:createSnapSVGPackage` | allows to build the `snapsvg.zip` archive |

### Set up your environment

#### Environment variable

In order to build SlideshowFX, you will need to set `JAVA_HOME` to point to your JDK `${jdk_version}` installation. Ensure the variable the present in the `PATH` environment variable.

#### Running SlideshowFX in your IDE

If you are contributing to SlideshowFX and developing some features, you probably use an IDE ([IntelliJ IDEA](http://www.jetbrains.com/idea/), [NetBeans](https://netbeans.org/), [eclipse](http://www.eclipse.org/), ...).

In order to start the application from your IDE, you can start the `com.twasyl.slideshowfx.app.SlideshowFX` class.

### Creating templates

Each presentation done with SlideshowFX is based on a _template_. A template is composed by four main parts:

- A _template configuration_ file which contains the configuration of the template. This file **must be** named `template-config.json` and is written using JSON ;
- A _template file_ which is the HTML page that will host all slides of the presentation ;
- A _sample file_ which is an HTML page providing a sample of the template ;
- _Slide's template files_ which are the template for each kind of slide the user can add in his presentation.

All of this content is archived in a file with the extension `.sfxt` (which stands for SlideshowFX template).

A typical template archive structure is the following:
```text
/
|- [F] template-config.json
|- [F] template.html
|- [F] sample.html
|- [D] resources
|- [D] slides
|------|- [D] template
```

Where:

- `[F]` = file
- `[D]` = directory

#### Template configuration file

The template configuration must be at the root of the archive and will contain all the configuration the template will need to load. An example is shown below:

```json
{
  "template" : {
    "name": "My first template",
    "version" : "0.2",
    "file" : "template.html",
    "js-object" : "sfx",
    "resources-directory" : "resources",

    "default-variables" : [
        {
            "name" : "author",
            "value" : "<content encoded in Base64>"
        },
        {
            "name" : "twitter",
            "value" : "<content encoded in Base64>"
        }
    ],

    "slides" : {
      "configuration" : {
        "slides-container" : "slideshowfx-slides-div",
        "slide-id-prefix" : "slide-",
        "template-directory" : "slides/template",
      },
      "slides-definition" : [
        {
          "id" : 1,
          "name" : "Title",
          "file" : "title.html",
          "elements" : [
            {
              "id" : 1,
              "html-id" : "\${slideNumber}-title",
              "default-content" : "Title"
            }, {
              "id" : 2,
              "html-id" : "\${slideNumber}-subtitle",
              "default-content" : "Subtitle"
            }, {
               "id" : 3,
               "html-id" : "\${slideNumber}-author",
               "default-content" : "Author"
            }, {
               "id" : 4,
               "html-id" : "\${slideNumber}-twitter",
               "default-content" : "@Twitter"
             }
          ]
        },
        {
          "id" : 2,
          "name" : "Title and content",
          "file" : "title_content.html",
          "elements" : [
            {
              "id" : 1,
              "html-id" : "\${slideNumber}-title",
              "default-content" : "Title"
            }, {
              "id" : 2,
              "html-id" : "\${slideNumber}-content",
              "default-content" : "Content"
            }
          ]
        },
        {
          "id" : 3,
          "name" : "Empty",
          "file" : "empty.html",
          "elements" : [
            {
              "id" : 1,
              "html-id" : "\${slideNumber}-content",
              "default-content" : "Content"
            }
          ],
          "dynamic-attributes" : [
            {
              "attribute" : "data-x",
              "template-expression" : "slideDataX",
              "prompt-message" : "Enter X position of the slide:"
            },
            {
              "attribute" : "data-y",
              "template-expression" : "slideDataY",
              "prompt-message" : "Enter Y position of the slide:"
            }
          ]
        }
      ]
    }
  }
}
```

The complete configuration is wrapped into a `template` JSON object. This object is described as below:

* `name`: the name of the template ;
* `version`: the version of the template ;
* `file`: the HTML file that is the template, which will host the slides ;
* `js-object`: is the name JavaScript object that will be used to callback to SlideshowFX ;
* `slides-container`: is the ID of the HTML markup that will contain the slides ;
* `resources-directory`: the folder that will contain the resources of the presentation, typically images file, sounds, etc ;
* `default-variables`: define custom variables that can be used inside the presentation. It is not mandatory ;
  * `name`: the name of the variable ;
  * `value`: the value of the variable encoded in Base64 ;
* `slides`: define the configuration of slides inside the presentation, as well as their template ;
  * `configuration`: JSON object that will contain the configuration of the slides ;
    * `slide-id-prefix`: is a prefix that will be placed in the ID attribute of an HTML slide element, prefixing the slide number ;
    * `template-directory`: the directory that will contain the slide’s templates ;
  * `slides-definition`: a JSON array that will contain the definition of each slide template as a JSON object with the following structure:
    * `id`: the ID of the slide ;
    * `name`: the name of the slide that will be displayed in SlideshowFX in the lst of available slide’s type ;
    * `file`: the template file of the slide ;
    * `elements`: a JSON array composed of JSON documents that describe all elements that can be dynamic modified (by the user or by SlideshowFX):
      * `id`: the ID for the element ;
      * `html-id`: the HTML ID of the element within the presentation. It can contain variable ;
      * `default-content`: the default content for the element ;
    * `dynamic-attributes`: a JSON array composed of JSON object describing the attributes that can be dynamically created when creating a slide by prompting its value to the user. Each object is structured as follow:
      * `attribute`: the name of the attribute ;
      * `template-expression`: the name of the template token. It is the Velocity token without the dollar sign ;
      * `prompt-message`: the message displayed to the user asking the value of the attribute.

**NOTE:** The best way for creating the template configuration file is to use the editor available in SlideshowFX.

#### Template file

The template file is the file that will host all slides, include all JavaScript libraries, CSS files and so on. In order to work, you have to:

- insert the freemarker token `\${sfxJavascriptResources}` inside a `script` code block
- define an ID for the HTML element that will host all slides
- insert the JavaScript function with the right implementation returning the current slide
  ```js
  function slideshowFXGetCurrentSlide() {
    // Return the current slide
  }
  ```
- insert the JavaScript function with the right implementation to go to a specified slide
  ```js
  function slideshowFXGotoSlide(slideId) {
    // Go to the slide identified by the given ID
  }
  ```
- insert the JavaScript function with the right implementation to go to the next slide
  ```js
  function slideshowFXNextSlide() {
    // Go to the next slide
  }
  ```
- insert the JavaScript function with the right implementation to go to the previous slide
  ```js
  function slideshowFXPreviousSlide() {
    // Go to the previous slide
  }
  ```

#### Slide’s template file

The template of a slide will define what HTML element a slide is. In some frameworks it will be a *section* markup, in others a *div* and so on. In order to create a template, you will have to respect some pre-requisites:

- The slide markup must have its ID attribute set to `\${slideIdPrefix}\${slideNumber}`
- Each element that is editable by the user must have an ID attribute composed of the slide number and a discriminator. An example of the title of the slide:
  ```html
  <h1 id="\${slideNumber}-title"></h1>
  ```
- Each element that is editable by the user must have the `ondblclick` attribute set to `\${sfxCallback}`
- It is strongly recommended to listen to slide changed event in the template file. When such an event is fired, you can call notify the SlideshowFX browser with the new current slide ID:
  ```js
  var slide = ...; // i.e.: document.getElementById(...)
  sfxBrowser.fireSlideChangedEvent(slide.id);
  ```
- If dynamic attributes are needed, they can be defined like the following. Note that for this example, template-expression are `slideDataX` and `slideDataY`
  ```html
  <section \${slideDataX} \${slideDataY}></section>
  ```
  
#### Valid template engine tokens

The following template engine tokens are available for SlideshowFX:

* `\${slideIdPrefix}` indicates the prefix that will be placed before the slide number for each slide ;
* `\${slideNumber}` indicates the slide number generated by SlideshowFX ;
* `\${sfxCallback}` indicates the function that will call SlideshowFX from JavaScript ;
* `\${sfxJavascriptResources}` indicates the JavaScript resources SlideshowFX will insert in the presentation, like the one for inserting content, calling a quiz etc.

### Presentations

Presentations made with SlideshowFX are an archive with the `.sfx` extension. The archive contains:

- The whole template structure
- The `presentation.html` file which is the whole presentation
- The `presentation-config.json` which is the whole configuration of the presentation

#### Configuration file

The configuration of the presentation is wrapped into an JSON configuration file named `presentation-config.json`. Here is a configuration example:
```json
{
  "presentation": {
    "id": 123456789,
    "custom-resources": [
        {
            "type": "<type>",
            "content": "<content encoded in Base64>"
        },
        {
           "type": "<type>",
           "content": "<content encoded in Base64>"
       }
    ],
    "variables": [
        {
            "name": "author",
            "value": "<content encoded in Base64>"
        },
        {
            "name": "twitter",
            "value": "<content encoded in Base64>"
        }
    ],
    "slides": [
      {
        "template-id": 1,
        "id": "slide-1400836547234",
        "number": "1400836547234",
        "speaker-notes": "<content encoded in Base64>",
        "elements": [
          {
            "template-id": 3,
            "element-id": "1400836547234-author",
            "original-content-code": "HTML",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          },
          {
            "template-id": 4,
            "element-id": "1400836547234-twitter",
            "original-content-code": "HTML",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          },
          {
            "template-id": 1,
            "element-id": "1400836547234-title",
            "original-content-code": "HTML",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          },
          {
            "template-id": 2,
            "element-id": "1400836547234-subtitle",
            "original-content-code": "HTML",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          }
        ]
      },
      {
        "template-id": 2,
        "id": "slide-1400836587307",
        "number": "1400836587307",
        "speaker-notes": "<content encoded in Base64>",
        "elements": [
          {
            "template-id": 1,
            "element-id": "1400836587307-title",
            "original-content-code": "HTML",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          },
          {
            "template-id": 2,
            "element-id": "1400836587307-content",
            "original-content-code": "TEXTILE",
            "original-content": "<content encoded in Base64>",
            "html-content": "<content encoded in Base64>"
          }
        ]
      }
    ]
  }
}
```

The `presentation` JSON object is described below:

* `custom-resources`: the JSON that will contain a JSON object for each custom resource of the presentation ;
  * `type`: the type of the resource. Possible values are `JAVASCRIPT_FILE`, `CSS_FILE`, `SCRIPT` and `CSS` ;
  * `content`: the content of the resource encoded in Base64 ;
* `variables`: define custom variables that can be used inside the presentation. It is not mandatory ;
  * `name`: the name of the variable ;
  * `value`: the value of the variable encoded in Base64 ;
* `slides`: the JSON array that will contain a JSON object for each slide of the presentation ;
  * `template-id`: the ID of the Slide that serves as template ;
  * `id`: the ID of the slide ;
  * `number`: the slide number ;
  * `elements`: the array containing a JSON object for each element defined in the slide ;
    * `template-id`: the ID of the slide element in the template ;
    * `element-id`: the ID of the slide element ;
    * `original-content-code`: the code of the markup syntax used ;
    * `original-content`: the original content of the element encoded in Base64. This syntax of the content must correspond to the content code ;
    * `html-content`: the original content converted in HTML encoded in Base64.

**NOTE:** This file shouldn't be modified manually as it is generated and overwritten by the application.
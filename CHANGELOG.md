# Changelog

Changes to the SlideshowFX software are listed by version within this document.

## Versions

### Version @@NEXT_VERSION@@

#### New and noteworthy

* Upgrade FontAwesome to version 5.9.0
* Upgrade vert.x to version 3.7.1
* Upgrade Box hosting connector internal library
* Upgrade Dropbox hosting connector internal library
* Upgrade Google Drive hosting connector internal library
* Upgrade the alert extension internal library
* Upgrade the code extension internal library
* Upgrade the snippet extension internal library
* Upgrade the asciidoctor markup extension internal library
* Introducing a presentation outline
* New theme functionality
* Uses ACE 1.4.5 in the slide content editor
* Support Ruby executable snippets
* Image extension allows to specify image's dimensions
* Image extension memorize the latest used folder
* Plugins packaging and architecture have changed

#### Bug fixes

* ALT key doesn't focus menu bar, but ALT GR does [#34](https://github.com/twasyl/SlideshowFX/issues/34)
* Tooltips are always initialized for plugin buttons in the setup [#35](https://github.com/twasyl/SlideshowFX/issues/35)
* Fix answered icon in attendee's chat
* Fix the author of messages in attendee's chat : for self messages, "I" is displayed instead of the login name
* Fix Zip Slip vulnerability

#### Breaking change

* SlideshowFX plugins are now distributed as a *.sfx-plugin. Previous plugins versions are unsupported and can be safely removed from any installation

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.2 |
| SlideshowFX-asciidoctor | 1.1 |
| SlideshowFX-box-hosting-connector | 1.2 |
| SlideshowFX-code-extension | 1.2 |
| SlideshowFX-drive-hosting-connector | 1.3 |
| SlideshowFX-dropbox-hosting-connector | 1.2 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.3 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.1 |
| SlideshowFX-link-extension | 1.2 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.1 |
| SlideshowFX-quote-extension | 1.1 |
| SlideshowFX-ruby-executor | 1.0 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.1 |
| SlideshowFX-shape-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.2 |
| SlideshowFX-textile | 1.2 |

### Version 2.0

#### New and noteworthy

* Update the SlideshowFX-textile plugin [#32](https://github.com/twasyl/SlideshowFX/issues/32)
* Update the code and snippet extension to support new programming language
* Alert content extension plugin uses new version of SweetAlert
* New Shape plugin allowing to insert shapes in a presentation
* Change the Windows default installation location
* Take the log configuration in consideration
* Creating a new presentation displays a template's library allowing to create presentations from previously used templates
* Display the current time in the information pane during a slideshow
* Adding support for speaker notes
* Ask for confirmation when deleting a presentation's variable
* Introduction of version for templates
* Allow to re-open recent presentations
* Uses ACE 1.3.1 in the slide content editor
* Using FontAwesome 5.0.8
* Twitter integration has been improved

#### Bug fixes

* Information screen during slideshow react to events emitted by the slideshow, meaning it will react correctly to display current and next slide
* Free information pane's resources correctly when exiting the slideshow mode
* Box hosting connector overwrite an existing file correctly when asked to
* Prevent NullPointerException to be thrown when auto saving is enabled and the presentation hasn't been already saved
* Allow Kotlin snippets to be executed on Windows

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.2 |
| SlideshowFX-asciidoctor | 1.1 |
| SlideshowFX-box-hosting-connector | 1.2 |
| SlideshowFX-code-extension | 1.2 |
| SlideshowFX-drive-hosting-connector | 1.3 |
| SlideshowFX-dropbox-hosting-connector | 1.2 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.3 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.1 |
| SlideshowFX-link-extension | 1.2 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.1 |
| SlideshowFX-quote-extension | 1.1 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.1 |
| SlideshowFX-shape-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.2 |
| SlideshowFX-textile | 1.2 |

### Version 1.4

#### New and noteworthy

* Create files and directories directly from the tree view of the template builder [#26](https://github.com/twasyl/SlideshowFX/issues/26)
* Display the application version in the splash screen [#29](https://github.com/twasyl/SlideshowFX/issues/29)
* Go to the newly added slide when adding a slide [#30](https://github.com/twasyl/SlideshowFX/issues/30)

#### Bug fixes

* The help can be displayed [#24](https://github.com/twasyl/SlideshowFX/issues/24)
* The label for the slides' template directory within the template builder has been corrected [#25](https://github.com/twasyl/SlideshowFX/issues/25)
* Allow to load the template's configuration when a slide has no template elements [#27](https://github.com/twasyl/SlideshowFX/issues/27)
* Allow to insert a slide that has no template elements in a presentation [#28](https://github.com/twasyl/SlideshowFX/issues/28)

### Version 1.3

#### New and noteworthy

* Code extension now supports the markdown syntax
* Code extension has been updated to support more languages
* Code extension's UI has been changed to allow better language's selection
* Link extension now supports the markdown syntax
* Remove support for LeapMotion
* The editor for the template configuration file inside the template builder has been improved [#21](https://github.com/twasyl/SlideshowFX/issues/21)
* The treeview inside the template builder only expands the root element at the opening
* The image extension allows to delete images stored within the presentation [#22](https://github.com/twasyl/SlideshowFX/issues/22)
* If no image is selected in the image extension, nothing is inserted within the slide editor
* Uses ACE 1.2.6 in the slide content editor
* Update libraries

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.1 |
| SlideshowFX-asciidoctor | 1.0 |
| SlideshowFX-box-hosting-connector | 1.1 |
| SlideshowFX-code-extension | 1.1 |
| SlideshowFX-drive-hosting-connector | 1.2 |
| SlideshowFX-dropbox-hosting-connector | 1.1 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.2 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.0 |
| SlideshowFX-link-extension | 1.1 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.0 |
| SlideshowFX-quote-extension | 1.0 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.1 |
| SlideshowFX-textile | 1.1 |

### Version 1.2

#### New and noteworthy

* Google Drive hosting connector is now working correctly with the v3 API [#12](https://github.com/twasyl/SlideshowFX/issues/12)
* It is possible to open files in the internal browser
* Ask for confirmation before deleting a slide
* The OSGi manager only starts the most recent plugins [#15](https://github.com/twasyl/SlideshowFX/issues/15)
* The image extension now supports the markdown markup

#### Bug fixes

* Correct the setup for the Linux platform which wasn't working [#14](https://github.com/twasyl/SlideshowFX/pull/14)

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.0 |
| SlideshowFX-asciidoctor | 1.0 |
| SlideshowFX-box-hosting-connector | 1.1 |
| SlideshowFX-code-extension | 1.0 |
| SlideshowFX-drive-hosting-connector | 1.1 |
| SlideshowFX-dropbox-hosting-connector | 1.0 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.1 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.0 |
| SlideshowFX-link-extension | 1.0 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.0 |
| SlideshowFX-quote-extension | 1.0 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.1 |
| SlideshowFX-textile | 1.0 |

### Version 1.1

#### New and noteworthy

* Box hosting connector allowing to interact with box [#8](https://github.com/twasyl/SlideshowFX/issues/8)
* Introduce the plugin center allowing to install and remove plugins [#9](https://github.com/twasyl/SlideshowFX/issues/9)
* Uses ACE 1.2.5 in the slide content editor
* Improve the setup in order to make the selection of at least one markup plugin mandatory [#11](https://github.com/twasyl/SlideshowFX/issues/11)

#### Bug fixes

* Pasting in the slide content editor doesn't paste twice anymore [#10](https://github.com/twasyl/SlideshowFX/issues/10)

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.0 |
| SlideshowFX-asciidoctor | 1.0 |
| SlideshowFX-box-hosting-connector | 1.0 |
| SlideshowFX-code-extension | 1.0 |
| SlideshowFX-drive-hosting-connector | 1.0 |
| SlideshowFX-dropbox-hosting-connector | 1.0 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.0 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.0 |
| SlideshowFX-link-extension | 1.0 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.0 |
| SlideshowFX-quote-extension | 1.0 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.0 |
| SlideshowFX-textile | 1.0 |

### Version 1.0

This is the first version of SlideshowFX.

#### Included plugins within the packaging

| Plugin | Version |
| ------ | ------- |
| SlideshowFX-alert-extension | 1.0 |
| SlideshowFX-asciidoctor | 1.0 |
| SlideshowFX-code-extension | 1.0 |
| SlideshowFX-drive-hosting-connector | 1.0 |
| SlideshowFX-dropbox-hosting-connector | 1.0 |
| SlideshowFX-go-executor | 1.0 |
| SlideshowFX-golo-executor | 1.0 |
| SlideshowFX-groovy-executor | 1.0 |
| SlideshowFX-html | 1.0 |
| SlideshowFX-image-extension | 1.0 |
| SlideshowFX-java-executor | 1.0 |
| SlideshowFX-javascript-executor | 1.0 |
| SlideshowFX-kotlin-executor | 1.0 |
| SlideshowFX-link-extension | 1.0 |
| SlideshowFX-markdown | 1.0 |
| SlideshowFX-quiz-extension | 1.0 |
| SlideshowFX-quote-extension | 1.0 |
| SlideshowFX-scala-executor | 1.0 |
| SlideshowFX-sequence-diagram-extension | 1.0 |
| SlideshowFX-snippet-extension | 1.0 |
| SlideshowFX-textile | 1.0 |
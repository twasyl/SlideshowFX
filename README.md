# What is SlideshowFX ?

![](https://github.com/twasyl/SlideshowFX/workflows/CI/badge.svg?branch=master)

SlideshowFX is a tool allowing you to create slide decks using HTML5 frameworks. Maybe have you heard about 
[reveal.js](http://lab.hakim.se/reveal-js/) or [impress.js](https://github.com/bartaz/impress.js/) that allow you to 
create beautiful slideshows, entirely customizable by using HTML5 and CSS3 technologies.

The main advantage is you don't need anything else than a text editor and a browser, both tools that can be find for free. 
Moreover, even if tools like PowerPoint and Keynote are mostly used and are really interesting, if you are a geek, 
you'll fall in love with creating HTML5 slideshows. It brings you a lot of freedom like adding a YouTube video right 
inside your presentation, playing music, automatically syntax highlighting code example and more.

### But why SlideshowFX ?

Even if you like web technologies, it could be long to completely create your presentation by writing HTML code. The 
goal of SlideshowFX is to facilitate that task by allowing you to use a template and then simply set titles, content, 
images, copying slides, moving them and more. All with a nice and simple interface. And because you may not want to 
write HTML code, you will also be able to set the content using Textile, Markdown and Asciidoctor. It's up to you.

But SlideshowFX also brings features that aren't provided by existing tools. Indeed you can start a chat with which your
attendees can ask you questions that are resend to all others attendees connected to the chat. But questions could also 
be asked through Twitter with a hashtag you define.

Are you a big lover of quizs? If yes you can even create quizs in your presentations that attendees can answer to and 
results will be displayed live! Cool, isn't it?

And developers aren't forgotten in SlideshowFX! Sometimes you display code snippets right? You would like to see the 
results right? In SlideshowFX, insert executable code snippets and see the console's output directly in your 
presentation! Currently you can create [Go](https://golang.org/), [golo](http://golo-lang.org/), 
[groovy](http://www.groovy-lang.org/), [Java](https://www.oracle.com/java/index.html), JavaScript, 
[kotlin](https://kotlinlang.org/), [Ruby](https://www.ruby-lang.org) and [scala](http://www.scala-lang.org/).

With SlideshowFX, bring your presentation to the next level of interactivity.

# How to get started

You can build SlideshowFX manually in order to test it. In order to do so, you will need:

* A JDK 14;
* The `JAVA_HOME` system variable set to point to your JDK installation.

## Building the application

Then in order to build the application:

* Open a command line in the repository folder ;
* Execute the command `gradlew :slideshowfx-setup:distZip` and wait for it to finish successfully ;
* Go in `REPOSITORY_HOME/slideshowfx-setup/build/distributions` in order to find the `SlideshowFX-<version>-<paltform>.zip` archive containing the SlideshowFX installer

## Running development version

To run the application, you can run the following command:

```shell script
gradlew -q :slideshowfx-app:runApplication
``` 
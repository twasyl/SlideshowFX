/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.content.extension.code.enums;

/**
 * This enumeration represents languages that are supported for the Code content extension.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public enum SupportedLanguage {
    ONEC("1C", "1c"),
    ACTIONSCRIPT("ActionScript", "actionscript"),
    APACHE("Apache", "apache"),
    APPLESCRIPT("AppleScript", "applescript"),
    ASCIIDOC("AsciiDoc", "asciidoc"),
    AUTOHOTKEY("AutoHotKey", "autohotkey"),
    AVR_ASSEMBLER("AVR Assembler", "avrasm"),
    AXAPTA("Axapta", "axapta"),
    BASH("Bash", "bash"),
    BRAINFUCK("BrainFuck", "brainfuck"),
    CSHARP("C#", "cs"),
    CAP_AND_PROTO("Cap'n Proto", "capnproto"),
    CLOJURE("Clojure", "clojure"),
    CMAKE("CMake", "cmake"),
    COFFEESCRIPT("CoffeeScript", "coffeescript"),
    CPP("C++", "cpp"),
    CSS("CSS", "css"),
    D("D", "d"),
    DART("Dart", "dart"),
    DELPHI("Delphi", "delphi"),
    DIFF("Diff", "diff"),
    DJANGO("Django templates", "django"),
    DOS_BATCH_FILE("DOS batch files", "dos"),
    DUST_JS("Dust.js", "dust"),
    ELIXIR("Elixir", "elixir"),
    ERLANG("Erlang", "erlang"),
    ERLANG_REPL("Erlang-REPL", "erlang-repl"),
    FSHARP("F#", "fsharp"),
    FIX("FIX", "fix"),
    GCODE("G-code", "gcode"),
    GHERKIN("Gherkin", "gherkin"),
    GLSL("GLSL", "glsl"),
    GO("Go", "go"),
    GRADLE("Gradle", "gradle"),
    GROOVY("Groovy", "groovy"),
    HAML("Haml", "haml"),
    HANDLEBARS("handlebars", "handlebars"),
    HASKELL("Haskell", "haskell"),
    HAXE("Haxe", "haxe"),
    HTML_CSS_JAVASCRIPT("HTML with CSS and JavaScript", "xml"),
    HTTP("HTTP", "http"),
    INI_FILE("Ini file", "ini"),
    JAVA("Java", "java"),
    JAVASCRIPT("JavaScript", "javascript"),
    JSON("JSON", "json"),
    LASSO("Lasso", "lasso"),
    LISP("Lisp", "lisp"),
    LIVECODE_SERVER("LiveCode Server", "livecodeserver"),
    LUA("Lua", "lua"),
    MAKEFILE("Makefile", "makefile"),
    MARKDOWN("Markdown", "markdown"),
    MATHEMATICA("Mathematica", "mathematica"),
    MATLAB("Matlab", "matlab"),
    MEL("MEL", "mel"),
    MIZAR("Mizar", "mizar"),
    MONKEY("Monkey", "monkey"),
    NGINX("nginx", "nginx"),
    NIMROD("Nimrod", "nimrod"),
    NIX("Nix", "nix"),
    NSIS("NSIS", "nsis"),
    OBJECTIVE_C("Objective C", "objectivec"),
    OCAML("OCaml", "ocaml"),
    ORACLE_RULES_LANGUAGE("Oracle Rules Language", "ruleslanguage"),
    OXYGENE("Oxygene", "oxygene"),
    PARSER_3("Parser 3", "parser3"),
    PERL("Perl", "perl"),
    PHP("PHP", "php"),
    PROTOCOL_BUFFERS("Protocol Buffers", "protobuf"),
    PYTHON("Python", "python"),
    PYTHON_PROFILER_OUTPUT("Python's profiler output", "profile"),
    Q_KDBP("Q/KDB+", "q"),
    R("R", "r"),
    RENDERMAN_RIB("RenderMan RIB", "rib"),
    RENDERMAN_RSL("RenderMan RSL", "rsl"),
    RUBY("Ruby", "ruby"),
    RUST("Rust", "rust"),
    SCALE("Scala", "scala"),
    SCHEME("Scheme", "scheme"),
    SCILAB("Scilab", "scilab"),
    SCSS("SCSS", "scss"),
    SMALLTALK("SmallTalk", "smalltalk"),
    SQL("SQL", "sql"),
    SWIFT("Swift", "swift"),
    TEX("TeX", "tex"),
    THRIFT("Thrift", "thrift"),
    TYPESCRIPT("TypeScript", "typescript"),
    VALA("Vala", "vala"),
    VB_NET("VB.NET", "vbnet"),
    VBSCRIPT("VBScript", "vbscript"),
    VHDL("VHDL", "vhdl"),
    VIM_SCRIPT("Vim Script", "vim"),
    X86ASM("x86asm", "x86asm"),
    XML("XML", "xml");

    private String name;
    private String cssClass;

    private SupportedLanguage(String name, String cssClass) {
        this.name = name;
        this.cssClass = cssClass;
    }

    /**
     * Get the name of the SupportedLanguage. This name could be displayed in a UI.
     * @return The name of the supported language.
     */
    public String getName() { return name; }

    /**
     * Get the CSS class associated to this SupportedLanguage.
     * @return The CSS class of this supported language
     */
    public String getCssClass() { return cssClass; }

    public static SupportedLanguage fromName(String name) {
        SupportedLanguage sl = null;

        for(SupportedLanguage s: SupportedLanguage.values()) {
            if(s.getName().equals(name)) {
                sl = s;
                break;
            }
        }

        return sl;
    }
}

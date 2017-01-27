package com.twasyl.slideshowfx.content.extension.code.enums;

/**
 * This enumeration represents languages that are supported for the Code content extension.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public enum SupportedLanguage {
    ABAP("ABAP", "abap"),
    ACTION_SCRIPT("ActionScript", "actionscript"),
    ADA("Ada", "ada"),
    APACHE_CONFIGURATION("Apache configuration", "apacheconf"),
    APL("APL", "apl"),
    APPLE_SCRIPT("AppleScript", "applescript"),
    ASCIIDOC("AsciiDoc", "asciidoc"),
    ASP_NET("ASP.NET", "aspnet"),
    AUTO_IT("AutoIt", "autoit"),
    AUTO_HOTKEY("AutoHotkey", "autohotkey"),
    BASH("Bash", "bash"),
    BASIC("BASIC", "basic"),
    BATCH("Batch", "batch"),
    BISON("Bison", "bison"),
    BRAINFUCK("Brainfuck", "brainfuck"),
    BRO("Bro", "bro"),
    C("C", "c"),
    C_LIKE("C-like", "clike"),
    COFFEE_SCRIPT("Coffee Script", "coffeescript"),
    CPP("C++", "cpp"),
    CRYSTAL("Crystal", "crystal"),
    CSHARP("C#", "csharp"),
    CSS("CSS", "css"),
    CSS_EXTRAS("CSS Extras", "css-extras"),
    D("D", "d"),
    DART("Dart", "dart"),
    DIFF("Diff", "diff"),
    DOCKER("Docker", "docker"),
    EIFFEL("Eiffel", "eiffel"),
    ELIXIR("Elixir", "elixir"),
    ERLANG("Erlang", "erlang"),
    FORTRAN("Fortran", "fortran"),
    FHSARP("F#", "fsharp"),
    GHERKIN("Gherkin", "gherkin"),
    GIT("git", "git"),
    GLSL("GLSL", "glsl"),
    GO("Go", "go"),
    GRAPH_QL("GraphQL", "graphql"),
    GROOVY("Groovy", "groovy"),
    HAML("Haml", "haml"),
    HANDLEBARS("Handlebars", "handlebars"),
    HASKELL("Haskell", "haskell"),
    HAXE("Haxe", "haxe"),
    HTML("HTML", "html"),
    HTTP("HTTP", "http"),
    ICON("Icon", "icon"),
    INFORM_7("Inform 7", "inform"),
    INI("Ini", "ini"),
    J("J", "j"),
    JADE("Jade", "jade"),
    JAVA("Java", "java"),
    JAVASCRIPT("JavaScript", "javascript"),
    JOLIE("Jolie", "jolie"),
    JSON("JSON", "json"),
    JULIA("Julia", "julia"),
    KEYMAN("Keyman", "keyman"),
    KOTLIN("Kotlin", "kotlin"),
    LATEX("LateX", "latex"),
    LESS("Less", "less"),
    LIVE_SCRIPT("LiveScript", "livescript"),
    LOLCODE("LOLCODE", "lolcode"),
    LUA("Lua", "lua"),
    MAKEFILE("Makefile", "makefile"),
    MARDOWN("Markdown", "markdown"),
    MARKUP("Markup", "markup"),
    MATLAB("MATLAB", "matlab"),
    MEL("MEL", "mel"),
    MIZAR("Mizar", "mizar"),
    MONKEY("Monkey", "monkey"),
    NASM("NASM", "nasm"),
    NGINX("nginx", "nginx"),
    NIM("Nim", "nim"),
    NIX("Nix", "nix"),
    NSIS("NSIS", "nsis"),
    OBJECTIVE_C("Objective-C", "objectivec"),
    OCAML("OCaml", "ocaml"),
    OZ("Oz", "oz"),
    PARI_GP("PARI/GP", "parigp"),
    PARSER("Parser", "parser"),
    PASCAL("Pascal", "pascal"),
    PERL("Perl", "perl"),
    PHP("PHP", "php"),
    PHP_EXTRAS("PHP Extras", "php-extras"),
    POWER_SHELL("PowerShell", "powershell"),
    PROCESSING("Processing", "processing"),
    PROLOG("Prolog", "prolog"),
    PROPERTIES(".properties", "properties"),
    PROTOCOL_BUFFERS("Protocol Buffers", "protobuf"),
    PUPPET("Puppet", "puppet"),
    PURE("Pure", "pure"),
    PYTHON("Python", "python"),
    Q("Q", "q"),
    QORE("Qore", "qore"),
    R("R", "r"),
    REACT_JSX("React JSX", "jsx"),
    REASON("Reason", "reason"),
    REST("reST", "rest"),
    RIP("Rip", "rip"),
    ROBOCONF("Roboconf", "roboconf"),
    RUBY("Ruby", "ruby"),
    RUST("Rust", "rust"),
    SAS("SAS", "sas"),
    SASS_SASS("Saas (Sass)", "sass"),
    SASS_SCSS("Sass (Scss)", "scss"),
    SCALA("Scala", "scala"),
    SCHEME("Scheme", "scheme"),
    SMALLTALK("Smalltalk", "smalltalk"),
    SMARTY("Smarty", "smarty"),
    SQL("SQL", "sql"),
    STYLUS("Stylus", "stylus"),
    SWIFT("Swift", "swift"),
    TCL("Tcl", "tcl"),
    TEXTILE("Textile", "textile"),
    TWIG("Twig", "twig"),
    TYPE_SCRIPT("TypeScript", "typescript"),
    VERILOG("Verilog", "verilog"),
    VHDL("VHDL", "vhdl"),
    VIM("vim", "vim"),
    WIKI("Wiki markup", "wiki"),
    XOJO("Xojo (REALbasic)", "xojo"),
    YAML("YAML", "yaml");

    private static final String CSS_PREFIX = "language-";
    private String name;
    private String cssClass;

    private SupportedLanguage(String name, String cssClass) {
        this.name = name;
        this.cssClass = CSS_PREFIX.concat(cssClass);
    }

    /**
     * Get the name of the {@link SupportedLanguage}. This name could be displayed in a UI.
     *
     * @return The name of the supported language.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the CSS class associated to this {@link SupportedLanguage}.
     *
     * @return The CSS class of this supported language.
     */
    public String getCssClass() {
        return cssClass;
    }

    public static SupportedLanguage fromName(String name) {
        SupportedLanguage sl = null;

        for (SupportedLanguage s : SupportedLanguage.values()) {
            if (s.getName().equals(name)) {
                sl = s;
                break;
            }
        }

        return sl;
    }
}

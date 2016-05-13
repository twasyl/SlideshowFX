package com.twasyl.slideshowfx.content.extension.code.enums;

/**
 * This enumeration represents languages that are supported for the Code content extension.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum SupportedLanguage {
    ACTION_SCRIPT("ActionScript", "actionscript"),
    APACHE_CONFIGURATION("Apache configuration","apacheconf"),
    APPLE_SCRIPT("AppleScript", "applescript"),
    ASP_NET("ASP.NET", "aspnet"),
    AUTO_HOTKEY("AutoHotkey", "autohotkey"),
    BASH("Bash", "bash"),
    BRAINFUCK("Brainfuck", "brainfuck"),
    C("C", "c"),
    COFFEE_SCRIPT("Coffee Script", "coffeescript"),
    CPP("C++", "cpp"),
    CSHARP("C#", "csharp"),
    CSS("CSS", "css"),
    DART("Dart", "dart"),
    EIFFEL("Eiffel", "eiffel"),
    ERLANG("Erlang", "erlang"),
    FORTRAN("Fortran", "fortran"),
    FHSARP("F#", "fsharp"),
    GHERKIN("Gherkin", "gherkin"),
    GIT("git", "git"),
    GO("Go", "go"),
    GROOVY("Groovy", "groovy"),
    HAML("Haml", "haml"),
    HANDLEBARS("Handlebars", "handlebars"),
    HASKELL("Haskell", "haskell"),
    HTTP("HTTP", "http"),
    INI("Ini", "ini"),
    JADE("Jade", "jade"),
    JAVA("Java", "java"),
    JAVASCRIPT("JavaScript", "javascript"),
    JULIA("Julia", "julia"),
    KEYMAN("Keyman", "keyman"),
    LATEX("LateX", "latex"),
    LESS("Less", "less"),
    LOLCODE("LOLCODE", "lolcode"),
    MAKEFILE("Makefile", "makefile"),
    MARDOWN("Markdown", "markdown"),
    MATLAB("MATLAB", "matlab"),
    NASM("NASM", "nasm"),
    NSIS("NSIS", "nsis"),
    OBJECTIVE_C("Objective-C", "objectivec"),
    PASCAL("Pascal", "pascal"),
    PERL("Perl", "perl"),
    PHP("PHP", "php"),
    PHP_EXTRAS("PHP Extras", "php-extras"),
    POWER_SHELL("PowerShell", "powershell"),
    PYTHON("Python", "python"),
    R("R", "r"),
    REST("reST", "rest"),
    RIP("Rip", "rip"),
    RUBY("Ruby", "ruby"),
    RUST("Rust", "rust"),
    SASS("Saas", "sass"),
    SCALA("Scala", "scala"),
    SCHEME("Scheme", "scheme"),
    SCSS("Scss", "scss"),
    SMALLTALK("Smalltalk", "smalltalk"),
    SMARTY("Smarty", "smarty"),
    SQL("SQL", "sql"),
    STYLUS("Stylus", "stylus"),
    SWIFT("Swift", "swift"),
    TWIG("Twig", "twig"),
    TYPE_SCRIPT("TypeScript", "typescript"),
    VHDL("VHDL", "vhdl"),
    WIKI("Wiki markup", "wiki"),
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
     * @return The name of the supported language.
     */
    public String getName() { return name; }

    /**
     * Get the CSS class associated to this {@link SupportedLanguage}.
     * @return The CSS class of this supported language.
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

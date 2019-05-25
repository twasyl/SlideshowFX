Feature: Loading a template configuration
  Loading a template configuration should work properly

  Scenario Outline: Checking general configuration values
    Given a template
    When a valid template configuration is loaded
    Then the "<field>" is equal to "<value>" in the template configuration file

    Examples:
      | field                         | value               |
      | template name                 | My template         |
      | template version              | 0.1                 |
      | template file                 | template.html       |
      | js-object                     | sfx                 |
      | resources directory           | resources           |
      | slides container              | slides              |
      | slide ID prefix               | slide-              |
      | slides template directory     | slides/template     |

  Scenario Outline: Checking the number of variables of a template configuration
    Given a template
    When a valid template configuration is loaded
    Then the template defines <number_of_variables> default variable

    Examples:
      | number_of_variables |
      | 1                   |

  Scenario Outline: Checking variables of a template configuration
    Given a template
    When a valid template configuration is loaded
    Then a default variable named "<name>" with the value "<value>" exists

    Examples:
      | name   | value   |
      | author | Someone |

  Scenario Outline: Checking the number of slides of a template configuration
    Given a template
    When a valid template configuration is loaded
    Then the template defines <number_of_slides> slides

    Examples:
      | number_of_slides |
      | 2                |

  Scenario Outline: Checking slides template of a configuration
    Given a template
    When a valid template configuration is loaded
    Then there is a slide template with id <slide_id>
    And it's name is "<slide_name>"
    And it's file is "<slide_file>"
    And which defines <number_of_element> slide elements

    Examples:
      | slide_id | slide_name    | slide_file   | number_of_element |
      | 1        | Title         | title.html   | 2                 |
      | 2        | Regular slide | regular.html | 2                 |

  Scenario Outline: Checking slide elements of a slide template
    Given a template
    When a valid template configuration is loaded
    Then the slide template <slide_id> has a template element with the id <element_id>
    And it's HTML id is "<html_id>"
    And it's default content is "<default_content>"

    Examples:
      | slide_id | element_id | html_id                 | default_content |
      | 1        | 1          | ${slideNumber}-title    | Title           |
      | 1        | 2          | ${slideNumber}-subtitle | Subtitle        |
      | 2        | 1          | ${slideNumber}-title    | Title           |
      | 2        | 2          | ${slideNumber}-content  | Content         |

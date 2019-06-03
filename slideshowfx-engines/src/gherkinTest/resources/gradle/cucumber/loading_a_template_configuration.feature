Feature: Loading a template configuration
  Loading a template configuration should work properly

  Scenario: Checking general configuration values
    Given a template
    When a valid template configuration is loaded
    Then the following fields are defined in the template configuration file
      | Name                      | Value           |
      | template name             | My template     |
      | template version          | 0.1             |
      | template file             | template.html   |
      | js-object                 | sfx             |
      | resources directory       | resources       |
      | slides container          | slides          |
      | slide ID prefix           | slide-          |
      | slides template directory | slides/template |

  Scenario: Checking default variables of a template configuration
    Given a template
    When a valid template configuration is loaded
    Then these default variables are defined
      | Name   | Value   |
      | author | Someone |

  Scenario: Checking slides template of a configuration
    Given a template
    When a valid template configuration is loaded
    Then these slide templates exist
      | Slide template ID | Name          | File         |
      | 1                 | Title         | title.html   |
      | 2                 | Regular slide | regular.html |

  Scenario: Checking slide element templates of a slide template
    Given a template
    When a valid template configuration is loaded
    Then the slide templates have the following elements
      | Slide template ID | Element ID | HTML ID                 | Default content |
      | 1                 | 1          | ${slideNumber}-title    | Title           |
      | 1                 | 2          | ${slideNumber}-subtitle | Subtitle        |
      | 2                 | 1          | ${slideNumber}-title    | Title           |
      | 2                 | 2          | ${slideNumber}-content  | Content         |

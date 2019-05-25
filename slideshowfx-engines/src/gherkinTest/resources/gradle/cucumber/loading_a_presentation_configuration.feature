Feature: Loading a presentation configuration
  Loading a presentation configuration should work properly

  Scenario Outline: Checking general configuration values
    Given a presentation
    When a valid presentation configuration is loaded
    Then the "<field>" is equal to "<value>" in the presentation configuration file

    Examples:
      | field           | value |
      | presentation id | 1     |

  Scenario Outline: Checking the number of slides of a presentation configuration
    Given a presentation
    When a valid presentation configuration is loaded
    Then the presentation configuration file contains <number_of_slides> slide

    Examples:
      | number_of_slides |
      | 2                |

  Scenario: Checking a slide in the presentation configuration
    Given a presentation
    When a valid presentation configuration is loaded
    Then these slides exist
      | Slide ID | Slide number | Template ID |
      | slide-01 | 01           | 1           |
      | slide-02 | 02           | 2           |

  Scenario: Checking elements of a slide
    Given a presentation
    When a valid presentation configuration is loaded
    Then the slides have the following elements
      | Slide ID | Template ID | Element ID  | Original content code | Original content      | HTML content          |
      | slide-01 | 1           | 01-title    | HTML                  | Presentation title    | Presentation title    |
      | slide-01 | 2           | 01-subtitle | HTML                  | Presentation subtitle | Presentation subtitle |
      | slide-02 | 1           | 02-title    | HTML                  | Slide title           | Slide title           |
      | slide-02 | 2           | 02-content  | HTML                  | Slide content         | Slide content         |

  Scenario: Checking custom resources of a presentation
    Given a presentation
    When a valid presentation configuration is loaded
    Then these custom resources are defined
      | Type   | Content                                    |
      | SCRIPT | function hello() { console.log('Hello'); } |

  Scenario: Checking variables of a presentation
    Given a presentation
    When a valid presentation configuration is loaded
    Then these variables are defined
      | Name   | Value   |
      | author | Someone |
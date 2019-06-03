Feature: Loading a presentation configuration
  Loading a presentation configuration should work properly

  Scenario: Checking general configuration values
    Given a presentation
    When a valid presentation configuration is loaded
    Then the following field is defined in the presentation configuration file
      | Name            | Value |
      | presentation id | 1     |

  Scenario: Checking a slide in the presentation configuration
    Given a presentation
    When a valid presentation configuration is loaded
    Then these slides exist
      | Slide ID | Slide number | Template ID | Speaker notes |
      | slide-01 | 01           | 1           | Some notes    |
      | slide-02 | 02           | 2           |               |

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

  Scenario Outline: Getting sibling slide
    Given a presentation
    When a valid presentation configuration is loaded
    Then the slide "<sibling_slide_id>" is <position> the slide "<slide_id>"

    Examples:
      | sibling_slide_id | position | slide_id |
      | slide-02         | after    | slide-01 |
      | none             | after    | slide-02 |
      | none             | before   | slide-01 |
      | slide-01         | before   | slide-02 |

  Scenario Outline: Getting sibling slide when there are no slide
    Given a presentation
    When a valid presentation configuration is loaded
    And the presentation has no more slides
    Then there is no <position> slide

    Examples:
      | position |
      | previous |
      | next     |

  Scenario Outline: Getting first and last slides
    Given a presentation
    When a valid presentation configuration is loaded
    And the presentation <has_or_not> slides
    Then the <position> slide is "<slide_id>"

    Examples:
      | has_or_not | position | slide_id |
      | has        | first    | slide-01 |
      | has        | last     | slide-02 |
      | has no     | first    | none     |
      | has no     | last     | none     |
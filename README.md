# maple

A release branching tool to branch in house modules by a specific release version

## control flow

Run tool --> Triggers Scan and extract first party dependency from BOM
--> Compiles the list of first party dependencies

On List of first party dependencies:

- checkout @ version defined in BOM of project locally
- build project
- Create release branch, tag @ release version, push

## Things needed for project

- BOM representation of all dependencies consumed by the monolith
- Ability to build locally and push changes
- a matrix of Git projects (representing the dependencies in the bom)
- project creation tool and/or project managment tool

## Example command

```bash
maple -b bom.json -v '2021.1'
```

It can be this simple, so no need for extraneous business. 

## TODO Running
- [ ] Generate BOM
    - [ ] Use the lists to generate a test-bom
    - [ ] Run it in its own Container on the side
- [ ] Consume BOM to create fpd-list (first-party dependency)
- [ ] Pass fpd-list to Orchestrator
- [ ] for dep in fpd-list
    - [ ] checkout at version specified
    - [ ] build
    - [ ] if passing,
        - [ ] create branch, create tag, push
    - [ ] notify team of project status


# free-modelling-editor — Backlog

Features and evolutions to develop in the FreeModellingEditor (FME). Known bugs go to `KNOWN_DEFECTS.md` (identifiers
`FME-D-<n>`), not here.

Identifiers are stable and never reused: `FME-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## Architecture

### FME-F-1 — A generic core and one extension per technology  ·  `DONE`

**Problem.** FME only knew diagrams: the abstract free model and free model instance imported their Diagram subclasses
(`@Imports`), and the perspective, the module, the action initializer and the nature panel created Diagram objects
directly. Adding another technology meant editing all of them.

**Decision.** Extensions are modules of this repository, one per technology, each depending on its technology adapter
(a technology adapter never depends on FME, a higher layer). `fme-module` is the generic core and keeps its name;
`fme-diagram` is the Diagram extension.

**Done.**
- SPI `org.openflexo.fme.FMETechnologyExtension`, found by `ServiceLoader`
  (`META-INF/services/org.openflexo.fme.FMETechnologyExtension`) and served by `FMETechnologyExtensions`. An extension
  gives its technology adapter (activated by `FMEModule`), its PAMELA entities, its free model and free model instance
  classes, its create / instantiate actions, its icons, its action initializers, the module view of its free model
  instances and, optionally, the inspectors of its technology.
- The entities of the extensions reach the project model through
  `ProjectNatureFactory.getProjectNatureSpecificClasses()` (openflexo-core), overridden by
  `FreeModellingProjectNatureFactory`; the abstract entities no longer import any subclass.
- Everything that depends on diagram or diana moved to `fme-diagram`, package `org.openflexo.fme.diagram.*`, with its
  wizards, FIBs and tests: the Diagram free model and instance, the palettes and the editor, the actions on diagram
  elements, and the actions on NoneGR instances (`DeclareInstanceOfExistingConcept`, `DisconnectInstanceFromConcept`,
  `CreateNewConceptFromNoneConcept`, `AbstractInstantiateConcept`), NoneGR being a Diagram notion. XML element names
  are unchanged: existing projects load as before (`TestCreateFreeModel.testReloadProject`).
- The nature panel and the free model panel create and instantiate free models through the extensions (a chooser opens
  when several technologies are available).
- The unfinished PowerPoint free model (a copy of the Diagram one) and the dead screenshot code of `FMEModule` are gone.

**Follow-ups.**
- The localized keys of the Diagram extension still live in the dictionaries of the core.
- The inspector of the represented concept is one Swing component: with several extensions, it belongs to the inspector
  group of the last one displayed (`FMEPerspective.getInspectorPanelGroup`).
- The nature panel still shows the labels "New diagram", "Import ppt slide" (disabled) and "Import excel" (no action):
  to revisit with the screens of FME-F-2.

### FME-F-2 — PowerPoint extension (`fme-pptx`)  ·  `TODO`

**Problem.** Annotate an existing PowerPoint deck: select shapes in its slides and declare them as instances of new or
existing concepts of the conceptual model, without modifying the `.pptx`.

**Decisions so far.**
- One free model instance federates a whole slideshow.
- A shape that is not annotated stays a plain PowerPoint shape (no NoneGR): actions are focused on `PowerpointShape`,
  and a GR instance is created at annotation time.
- Glued connectors give instances of relational concepts; groups and nesting give containment.
- The view reuses the slideshow view of pptx-ta-ui (thumbnails, slide, selection) with an FME overlay.
- A concept instance can be represented both in a diagram and in a deck (the sample data is shared).

**Prerequisites in openflexo-pptx.** Shape identity that survives a reload (PPTX-F-5), the type of `PowerpointShapeRole`
(PPTX-D-4), and a contextual menu and decoration hooks in the slideshow view (PPTX-F-6).

**Next.** The screens are to be specified with the user before the plan.

### FME-F-3 — A generic layer of free-modelling actions  ·  `IN PROGRESS`

**Problem.** The actions that make concepts emerge from a representation are generic in intent, but today they are
written for diagrams and live in `fme-diagram`. The core should state the acts of free modelling once, and each
technology should only say how they apply to its own elements. This layer is expected to settle progressively, as
more technologies are explored (PowerPoint first, FME-F-2): no big-bang abstraction, but lift what two technologies
prove to share.

**Generic acts identified so far** (with their current Diagram implementation):
- make a new concept emerge from a representation element (`CreateNewConceptFromDiagramElement`,
  `CreateNewConceptFromNoneConcept`);
- declare a representation element as an instance of an existing concept (`DeclareInstanceOfExistingConcept`,
  `DeclareInstanceOfExistingConceptFromDiagramElement`);
- disconnect an instance from its concept (`DisconnectInstanceFromConcept`);
- make a relation emerge from a link between two elements (`CreateNewConceptFromDiagramConnector`,
  `CreateNewFMEPropertyFromDiagramConnector`);
- containment from the nesting of elements (drop scheme targets of the Diagram GR concepts).

**What a technology would provide** (first guess): the representation element of a GR instance and the GR instance of
an element, how to create the technology role of a GR concept and bind an element to it, what graphical information
moves between an element and its concept (the `GRStrategy` of the Diagram actions), how to refresh its view.

**Done (step 1, the first two candidates).**
- `AbstractInstantiateConcept` is back in the core (`org.openflexo.fme.model.action`), with the free model and free
  model instance of the technology as type parameters: `AbstractInstantiateConcept<A, FM, FMI>`.
- `AbstractDeclareInstanceOfExistingConcept<A, FM, FMI>` (core) holds the free modelling part of "declare an instance
  of an existing concept": the concept and the container, the conceptual instance created in the sample data, the GR
  concept retrieved or created, the focused instance moved to the GR concept and bound to the conceptual instance, the
  notifications of the free model instance. The technology answers three hooks: `prepareRepresentation` (before the
  instance changes of concept), `completeRepresentation` (after), `refreshRepresentation` (last, for its view), plus
  `conceptChanged`.
- The Diagram `DeclareInstanceOfExistingConcept` keeps its name, action type and wizard, and only holds the shape:
  the `GRStrategy`, the propagation of a redefined shape to the other instances, the palette element of a first
  instance, the invalidation of the drawing.

**Next candidates.**
- `DeclareInstanceOfExistingConceptFromDiagramElement` duplicates most of the declare routine, starting from a diagram
  element instead of a NoneGR instance: the "from a plain element" variant is the one PowerPoint needs (FME-F-2), so
  the template should also accept an element without GR instance yet.
- `CreateNewConceptFromNoneConcept` / `CreateNewConceptFromDiagramElement`, then `DisconnectInstanceFromConcept`, on
  the same pattern.

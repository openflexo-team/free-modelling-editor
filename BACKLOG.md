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
- Done (2026-11-03, with FME-F-2): the nature panel shows one button per technology, generated from the extensions (a
  GINA `Iteration`), labelled with the instantiation action of the technology ("New diagram", "Annotate a PowerPoint
  slideshow"); the dead buttons "Import ppt slide" and "Import excel" are gone. The button of the free model panel takes
  the label of the technology of its free model.

### FME-F-2 — PowerPoint extension (`fme-pptx`)  ·  `IN PROGRESS`

**Problem.** Annotate an existing PowerPoint deck: select shapes in its slides and declare them as instances of new or
existing concepts of the conceptual model, without modifying the `.pptx`.

**Decisions.**
- One free model instance federates a whole slideshow, referenced in place in its resource center (no copy); the `.pptx` is
  never modified nor saved.
- The two levels stay in the model (FML needs a virtual model for the instances, and the vocabulary is shared by all the
  decks of a project), but the PowerPoint free model is implicit: one per project, created with the first annotation, never
  offered to create or choose. "Annotate a PowerPoint slideshow" only asks for the slideshow; the annotation is named after
  it. A slideshow is annotated once in a project: annotating it again opens its annotation (decided 2026-11-03).
- A shape that is not annotated stays a plain PowerPoint shape (no NoneGR): a GR instance is created at annotation time, from
  the selected `PowerpointShape`. A shape carries one annotation at most.
- Actions on a shape: new concept, instance of an existing concept (a new instance, or one that already exists in the sample
  data, possibly represented in a diagram), disconnect. The name proposed for a new instance is the text of the shape (first
  line), or its PowerPoint name.
- Actions on a connector glued to two annotated shapes: new relational concept, new property. Disabled otherwise. Groups and
  nesting giving containment come later.
- Screens: the left column is the one of the Diagram extension; the center is the slideshow view of pptx-ta-ui (slide,
  thumbnails on its right) with an overlay marking annotated shapes (border colored by concept, badge with the concept
  name); the right column shows the browser of the shown slide on top, the inspector of the represented concept below (no
  PowerPoint inspector).
- A concept instance can be represented both in a diagram and in a deck (the sample data is shared).

**Plan.** Lot 1, the generic actions in the core and Diagram rebuilt on them (FME-F-3 step 2): done. Lot 2, the `fme-pptx`
module: done. Lot 3: the actions on glued connectors.

**Done (lot 2, 2026-11-03).**
- Module `fme-pptx` (`org.openflexo.fme.pptx`), extension `PowerpointFMEExtension`, in the products freemodelling and
  openflexo-designer.
- `FMEPowerpointFreeModel`: its virtual model has a `BasicPowerpointModelSlot` (`slideshow`), connected by the creation scheme
  to the slideshow given as parameter; each GR concept has a `PowerpointShapeRole` (`shape`). `FMEPowerpointFreeModelInstance`
  annotates a slideshow chosen among those of the resource centers, referenced where it is.
- Actions on a shape: new concept, instance of an existing concept (new or existing instance), disconnect, on the core
  templates. `PowerpointAnnotations` registers the connected free model instances, so that the actions, which only know the
  shape, are enabled for a shape of an annotated slideshow that carries no annotation yet (declare, new concept) or one
  (disconnect). Disconnecting deletes the instance standing for the shape without its deletion scheme, after detaching the
  shape and the conceptual instance.
- View `FMEPowerpointFreeModelModuleView`: the slideshow view of pptx-ta-ui with `ConceptDecorationProvider` (border of the
  color of the concept, by its rank in the conceptual model, and a badge with its name); the right column shows the browser
  of the shown slide and the inspector of the represented concept. An instance chosen in a browser of the left column brings
  the slide of its shape (the shape is not selected).
- Core: the inspector of the FreeModellingEditor shows what the extension answers for a selected object
  (`FMETechnologyExtension.getInspectedObject`): the instance a shape stands for, nothing for a shape that stands for none.
  The steps "choose the free model" and "describe the free model" of the wizards are generic components of the core
  (`ChooseFreeModel.fib`, `DescribeFreeModel.fib`), Diagram uses them too.
- Checked by `TestPowerpointFreeModel` on CycloCity (pptx-ta-test-rc): creation, instantiation, new concept, new and
  existing instance, overlay and inspected object, disconnection with and without orphan, save and reload (the annotations
  point to the same shapes, the slideshow resource is never modified; the bytes of the deck are compared only when it is a
  file, not inside a jar), plus the extension and the wizard component.

**Done (implicit free model, 2026-11-03).** `FMEPowerpointFreeModel.getFreeModel(nature)`; the creation action is disabled once
the project has one (the "+" of the free models no longer offers PowerPoint then); the instantiation action selects it or
creates it, names the annotation after the slideshow and returns the existing annotation of an already annotated slideshow;
its wizard has no "choose the free model" step (`AbstractInstantiateFMEFreeModelWizard.showsChooseFreeModel()`). In the
core, the step "choose the free model" now only lists the free models of the technology of the action
(`InstantiateFMEFreeModel.getAvailableFreeModels()`): it listed every free model of the nature, a Diagram wizard offering a
PowerPoint free model. Checked by `TestPowerpointFreeModel`.

**Not tried in the running application.** The contextual menu on a shape, the wizards, the overlay, the columns.

**Prerequisites in openflexo-pptx.** Shape identity that survives a reload (PPTX-F-5), the type of `PowerpointShapeRole`
(PPTX-D-4), and a contextual menu and decoration hooks in the slideshow view (PPTX-F-6): done; a `PowerpointShapeRole` whose
actor is never deleted nor cloned with its instance, and a slideshow view letting the embedding module place the browser of
the slide: done with lot 2.

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

**Done (step 2, every act but containment, 2026-11-03).**
- The actions are focused either on the instance of a GR concept standing for an element (the NoneGR of a diagram), or on
  the element itself (a diagram shape, a PowerPoint shape): `AbstractFreeModelInstanceAction<A, T, FM, FMI>` finds the
  free model instance of either. A free model instance says whether an element belongs to its artefact (`contains`), the
  instance standing for an element (`getGRInstance`) and the element of an instance (`getRepresentationElement`); the nature
  finds the free model instance of an element, and every representation of a conceptual instance across free model
  instances (`getRepresentations`).
- Templates in `org.openflexo.fme.model.action`, each with its generic wizard and initializer in the core:
  `AbstractDeclareInstanceOfExistingConcept` (the element may stand for a new instance, named after it, or for an instance
  already in the sample data), `AbstractCreateNewConceptFromElement`, `AbstractDisconnectInstanceFromConcept` (the
  conceptual instance is deleted only when nothing else represents it), `AbstractCreateNewConceptFromLink` and
  `AbstractCreateNewFMEPropertyFromLink` over `AbstractLinkAction`. A technology gives the instance standing for the
  element, the default name, the element containing it, and what happens to its representation; the declare wizard shows
  the extra options of the technology (the shape strategy for Diagram) from a component it gives.
- Every Diagram action is rebuilt on them; the "from diagram element" variants share `ShapeRepresentation` and
  `DiagramRepresentations` with the NoneGR ones. `FME-D-2` and `FME-D-3` fixed. Checked by `TestFreeModellingActions`.
- Behaviour changes for Diagram: declaring a NoneGR instance whose concept has no GR concept yet now gives the new GR
  concept the shape of the element, and takes the container of the enclosing shape, as the "from diagram element" variant
  did; "new concept" from a plain shape opens the wizard (it ran without a name before); a connector gives a relational
  concept through the wizard of `CreateNewRelationalConcept`. Not tried in the running application.

**Next candidates.**
- Containment from the nesting of elements, when PowerPoint groups need it.
- Deleting a GR instance (the anonymous deletion scheme of the GR concepts) still deletes the conceptual instance, even
  when another free model instance represents it.

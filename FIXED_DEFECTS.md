# free-modelling-editor — Fixed defects

Defects of free-modelling-editor that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

## Diagram actions

`FME-D-1` and `FME-D-2` were in `DeclareInstanceOfExistingConceptFromDiagramElement` ("declare a diagram element as an instance of an
existing concept"), rebuilt on the core template `AbstractDeclareInstanceOfExistingConcept` by step 2 of [`FME-F-3`](./BACKLOG.md).

### FME-D-1 — The representation strategy of "declare from diagram element" is bound to the wrong enum  ·  `DONE`

**Fixed (2026-11-03).** The action no longer declares its own `GRStrategy`: it uses the one of `DeclareInstanceOfExistingConcept`,
which the wizard's radio button list already iterates. Checked by compiling and by the FIB validation of the wizard, not by running
the wizard in the application.

**Symptom (expected).** In the wizard, choosing a representation strategy (get the concept's shape, redefine the concept's shape,
keep the shape) is not taken into account; the action keeps its default, `GetConceptShape`.

**What is verified (by reading, not by running).** `DeclareInstanceOfExistingConcept` and
`DeclareInstanceOfExistingConceptFromDiagramElement` each declare their own `GRStrategy` enum. The radio button list of
`Fib/Wizard/ConfigureNewInstanceOfExistingConceptFromDiagramElement.fib` iterates over
`DeclareInstanceOfExistingConcept$GRStrategy`, but its `data.grStrategy` is the wizard's property typed by
`DeclareInstanceOfExistingConceptFromDiagramElement.GRStrategy`. The values offered are not those the setter accepts, and the
current value is never among the values listed.

**Reproduction.** In a diagram free model with a concept whose GR concept already exists, select a plain shape, run "declare
instance of existing concept", pick "redefine shape of concept": the concept's shape is expected to stay unchanged.

**Fix direction.** A single `GRStrategy` enum shared by the two actions.

### FME-D-2 — "Declare from diagram element" fails when the proposed concept is kept  ·  `DONE`

**Fixed (2026-11-03).** The action is built on the core template, which only uses `getConcept()`. Checked by
`TestFreeModellingActions.testDeclarePlainShapeKeepingProposedConcept`: a plain shape is declared as an instance of the concept proposed
first, which has no GR concept yet; the GR concept, the instance and the palette element are created.

**Symptom (expected).** The action fails with a `NullPointerException` when the user keeps the concept the wizard proposes first,
and the GR concept of that concept does not exist yet (first element declared as an instance of it). The element is left half
declared: it has already moved to the new GR concept when the failure happens, and no palette element is created.

**What is verified (by reading, not by running).** `getConcept()` returns the first concept of the conceptual model while the
`concept` field is still null; the wizard calls `setConcept` only when the user changes the selection. When the GR concept is new,
`declareInstanceOfExistingConcept()` looks up the palette with the raw field (`b.getBoundFlexoConcept() == concept`), finds nothing,
and calls `createPaletteElementForConcept(grConcept, concept, ...)` with `concept == null`, which dereferences it
(`concept.getName()`) in `FMEDiagramFreeModel`.

**Reproduction.** In a diagram free model whose conceptual model holds one concept with no instance in the diagram, select a plain
shape, run "declare instance of existing concept" and validate the wizard without touching the concept field.

**Workaround.** Select another concept then the wanted one again, so that the field is set.

**Fix direction.** Use `getConcept()` instead of the field (the core template already does).

### FME-D-3 — A concept first declared through the NoneGR never got its palette element  ·  `DONE`

**Fixed (2026-11-03).** The palette element of a concept is bound to its GR concept (`createPaletteElementForConcept` gives the GR
concept to `CreateFMLControlledDiagramPaletteElement`), but both "declare" actions looked for it with the conceptual concept, so they
never found an existing one. In `DeclareInstanceOfExistingConcept` (from an instance of the NoneGR), the guard meant to detect a first
instance counted the instances of the conceptual concept in the free model instance, which holds none: when the declaration created the
GR concept, the concept got no element in the palette (by reading the former code, not reproduced). The "from diagram element" variant
created one whenever the GR concept was new, which happened to be right. Both now look for the element bound to the GR concept and create
one when there is none. Checked by `TestFreeModellingActions`: exactly one element after a first declaration, none added for a concept
that already has one.

# Common coding recipies

## Creating a new layer type

1. Add the type constant to `LayerType.ts`
2. Add the type icon to `LayersPanel.tsx`
3. Add an icon for the type in `Icon.tsx`

Next you want to implement the layer on the backend, so that clicking the layer icon will add it to the site,
and an event will be returned by to the frontend to render it.

4. Add code for the new type to the `NodeDetailsPanel` and go from there

When you have a design for the attributes of the new layer, implement them at the 
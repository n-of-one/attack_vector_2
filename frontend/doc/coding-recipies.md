# Common coding recipies

## Creating a new layer type

1. Add the type constant to `LayerTypes.ts`
2. Add the type icon to `LayersPanel.tsx`
3. Add an icon for the type in `Icon.tsx` (choose the family), and the icon itself in `Glyphicon.tsx` or `SvgIcon.tsx`
   (svg files live in `public/img/editor/tabler/`)

Next you want to implement the layer on the backend, so that clicking the layer icon will add it to the site,
and an event will be returned by to the frontend to render it.

4. Add code for the new type to the `NodeDetailsPanel`
5. Implement the LayerXYZ that you are adding. Ignore the backend for now, focus on the attributes that this layer needs.
   - Model in `common/model/layer/LayerXYZ.ts`, attributes in `common/sites/SiteModel.tsx` (`LayerDetails`)
   - Panel in `editor/component/map/panel/layer/type/panel/`
6. Then, implement it at the backend

### Other places that switch on layer type

These have a default branch, so TypeScript will not tell you they are missing a case:

- `hacker/run/component/scaninfo/LayerInfo.tsx`: what the hacker sees about this layer after a scan
- Layers with a text that can be edited with the fullscreen text editor and shown in a terminal preview:
  - `editor-text/EditorTextRootReducer.ts` (`textFromLayer`)
  - `editor/reducer/editorTerminalPreviewReducer.ts` (`forLayer`), otherwise the preview stays empty until the user types

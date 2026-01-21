---
sidebar_position: 2
---
# Advanced formatted text
Combining the options of formatting text with unicode characters, you can create very nice results like this:

![](../../../assets/gm/sites/advanced/advanced-formatting-2.png)

## LLM support
Hand crafting these is very cumbersome and error prone. But luckily, LLMs are quite decent at producing these for you. The following can be used as part of a prompt to explain to an LLM how to format text:

```
My system displays text on a dark theme, default color is light grey. The line length is 80 characters. It uses the following codes to add formatting or styling to text.
[primary]blue[/]
[pri]blue[/]
[ok]green[/]
[info]light blue[/]
[warn]orange[/]
[error]red[/]
[mute]dark gray[/]
[b]bold[/]
[i]italics[/]
[s]strikethrough[/]
[u]underline[/]
[us]underline strikethrough[/]

These cannot be nested, so this does not work [u][ok]text[/], as each new styling resets all previous stylings.
```

## Debugging
The current edit window is not ideal for 

## Examples
These are some more examples of texts used in the Frontier Larp.

### Example with minor formatting

![](../../../assets/gm/sites/advanced/advanced-formatting-1.png)

### Example with picture

![](../../../assets/gm/sites/advanced/advanced-formatting-3.png)

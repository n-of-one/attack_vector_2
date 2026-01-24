---
sidebar_position: 1
---
# Formatting text
Text that you prepare for your players will be displayed in their terminal. This can be plain text, but the terminal also supports formatting.

This text is the text that you put in the layers [text](../../../player/Layers#text) and [script interaction](../../../player/Layers#script-interaction) as well as in the script effect [show message](../../scripts/Script%20effects#show-message).

## Formatting tags
This text can be formatted with the following syntax:

| Input              | Output                              |
| ------------------ | ----------------------------------- |
| `test`             | test                                |
| `[primary]test[/]` | <span class="t_primary">test</span> |
| `[pri]test[/]`     | <span class="t_pri">test</span>     |
| `[ok]test[/]`      | <span class="t_ok">test</span>      |
| `[info]test[/]`    | <span class="t_info">test</span>    |
| `[warn]test[/]`    | <span class="t_warn">test</span>    |
| `[error]test[/]`   | <span class="t_error">test</span>   |
| `[mute]test[/]`    | <span class="t_mute">test</span>    |
| `[b]test[/]`       | <span class="t_b">test</span>       |
| `[i]test[/]`       | <span class="t_i">test</span>       |
| `[s]test[/]`       | <span class="t_s">test</span>       |
| `[u]test[/]`       | <span class="t_u">test</span>       |
| `[us]test[/]`      | <span class="t_us">test</span>      |

The last formatting option will override previous formatting options. So you cannot nest styles.

`[ok][u]test[/]`  will result in <span class="t_u">test</span>.

But you don't have to end a style before starting a new one.

`[ok]green[error]red[/]normal`  will result in <span class="t_ok">green</span><span class="t_error">red</span>normal.

## Links and images
Links to other websites and images are supported like this:

| Input                                                                           | Output                                                                                                  |
| ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| Link `[https://example.com]link[/]`                                             | <a href="https://example.com" target="_blank">link</a>                                                  |
| Image `!https://dummyimage.com/100x50/1e6ba1/fff!`                              | <img src="https://dummyimage.com/100x50/1e6ba1/fff"/>                                                   |
| Image link `[https://example.com]!https://dummyimage.com/100x50/1e6ba1/fff![/]` | <a href="https://example.com" target="_blank"><img src="https://dummyimage.com/100x50/1e6ba1/fff"/></a> |

You can style the text of link:

`[https://example.com][ok]link[/]` will result in <a href="https://example.com" target="_blank"><span class="t_ok">link</span></a>

## Full screen editor
Use the full screen editor when creating larger formatted text.
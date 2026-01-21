---
sidebar_position: 1
---
# Formatting text
Layers like the [text](../../../player/Layers#text) layer and the [script interaction](../../../player/Layers#script-interaction) layer allow the GM to prepare text to be displayed to players.

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

Links to other websites and images are supported like this:

| Input                                                                          | Output                                                                                                 |
| ------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| Link `[http://example.com]link[/]`                                             | <a href="http://example.com" target="_blank">link</a>                                                  |
| Image `!https://dummyimage.com/100x50/1e6ba1/fff!`                             | <img src="https://dummyimage.com/100x50/1e6ba1/fff"/>                                                  |
| Image link `[http://example.com]!https://dummyimage.com/100x50/1e6ba1/fff![/]` | <a href="http://example.com" target="_blank"><img src="https://dummyimage.com/100x50/1e6ba1/fff"/></a> |




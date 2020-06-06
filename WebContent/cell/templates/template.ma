
[top]
components : {name}

[{name}]
type : cell
dim : ({height},{width})
delay : transport
defaultDelayTime : {delay}
border : nonwrapped

{neighbors}
initialValue : {initial_value}
initialCellsValue : {val_file}
localtransition : rules

[rules]

% ...

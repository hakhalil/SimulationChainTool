
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
localtransition : rules

% 2 State Variables corresponding to CO2 concentraion in ppm (conc) and the kind of cell (type)
% Default CO2 concentration inside a building (conc) is 0.05% or 500ppm in normal air
StateVariables: conc type counter
NeighborPorts: c ty
StateValues: 500 -100 -1
InitialVariablesValue: {val_file}

[rules]

% ...

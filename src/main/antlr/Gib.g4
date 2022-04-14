grammar Gib;

@header {package com.github.ekenstein.gibson.parser;}

gib: header game;

game: '\\GS' game_property* '\\GE';
game_property
    : 'STO' INT moveNumber=INT player=INT x=INT y=INT #move
    | 'INI' INT INT handicap=INT '&' INT #ini
    | 'SKI' INT moveNumber=INT #pass
    | (INT INT '&' INT) #ignore1
    | (INT INT INT) #ignore2
    ;

header: '\\HS' header_property+ '\\HE';
header_property: '\\[' property_identifier VALUE?;
property_identifier : 'GIBOKIND'
                    | 'TYPE'
                    | 'SZAUDIO'
                    | 'GAMECONDITION'
                    | 'GAMETIME'
                    | 'GAMERESULT'
                    | 'GAMEZIPSU'
                    | 'GAMEDUM'
                    | 'GAMEGONGJE'
                    | 'GAMETOTALNUM'
                    | 'GAMENAME'
                    | 'GAMEDATE'
                    | 'GAMEPLACE'
                    | 'GAMELECNAME'
                    | 'GAMEWHITENAME'
                    | 'GAMEWHITELEVEL'
                    | 'GAMEWHITENICK'
                    | 'GAMEWHITECOUNTRY'
                    | 'GAMEWAVATA'
                    | 'GAMEWIMAGE'
                    | 'GAMEBLACKNAME'
                    | 'GAMEBLACKLEVEL'
                    | 'GAMEBLACKNICK'
                    | 'GAMEBLACKCOUNTRY'
                    | 'GAMEBAVATA'
                    | 'GAMEBIMAGE'
                    | 'GAMECOMMENT'
                    | 'GAMEINFOMAIN'
                    | 'GAMEINFOSUB'
                    | 'WUSERINFO'
                    | 'BUSERINFO'
                    | 'GAMETAG'
                    ;

/*
 * Lexer rules
 */
WS:  [ \n\r]+ -> skip;

LBRACKET: '\\[';
RBRACKET: '\\]';
VALUE: '=' .*? '\\]';
NONE: EQ RBRACKET;
INT: '0' | [1-9] [0-9]*;

fragment EQ : '=';

\version "2.14.1"
\include "english.ly"

\score {
\new Staff <<
    \new Voice \relative c {
    \set midiInstrument = #"violin"
    \voiceOne
     \key c \major
    \time 2/4
     c8 c16 c16 c8 c16 c16
     c8 f='8 a8 c8 b8
     b16 b16 b8 b16 b16
     b8 e='8 g='8 b8
     c8 c16 c16 c8 c16 c16
     c8 ds8 e8 f8
     e8 c8 b8 g='8
     f4 f8 r8
    }
>>
\layout { }
  \midi {
    \context {
      \Staff
    }
    \tempo 4 = 60
  }
}

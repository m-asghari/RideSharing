reset
set size 1.2,1.0
set boxwidth 0.6 relative
set style fill solid
set key left top
set style fill pattern border -1
set grid
set datafile separator "\t"
set xtics   ("APART_T" 1, "SP" 2, "APART_R" 3)
#set key horizon top at screen 0.98, graph 1.1
#set key reverse Left left top at screen 1.12, graph 1
#set key font ",20" spacing 1.01
set style line 1 lt 1 lc rgb "#0000ff" lw 4 pt 3 ps 4.5
set style line 2 lt 1 lc rgb "#ee7600" lw 3 pt 6 ps 4.5
set style line 3 lt 1 lc rgb "#007b35" lw 3 pt 8 ps 4.5
set style line 4 lt 1 lc rgb "#33ccff" lw 3 pt 12 ps 4.5
set style line 5 lt 1 lc rgb "red" lw 3 pt 1 ps 4.5
set style line 6 lt 1 lc rgb "dark-red" lw 3 pt 4 ps 4.5
set style line 7 lt 1 lc rgb "#000066" lw 3 pt 5 ps 4.5
set style line 8 lt 1 lc rgb "#ff00ff" lw 3 pt 9 ps 4.5
set style line 9 lt 1 lc rgb "#0fd3f5" lw 3 pt 7 ps 4.5
set style line 10 lt 1 lc rgb "#dd6500" lw 3 pt 11 ps 2.5
set style line 11 lt 1 lc rgb "#ffccff" lw 3 pt 13 ps 2.5
set style line 12 lt 1 lc rgb "#ffffcc" lw 3 pt 7 ps 2.5
set terminal postscript eps enhanced color "Helvetica" 32
set yrange [80:100]
set y2range [0:30]
set ylabel 'Shared rides (%)'
set y2label 'Avg Detour (%)'
set y2tics (0,10,20,30)
set output 'quality.eps'
plot 'detour_baseline.txt' using 1:2 ti 'Avg Detour' with lines ls 1 axes x1y2, \
'' using 1:3 ti 'Shared rides' with boxes ls 5 axes x1y1
clear

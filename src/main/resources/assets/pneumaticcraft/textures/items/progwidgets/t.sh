file *.png | grep "256 x 256" | cut -d: -f1 | while read i; do
   convert $i -verbose -interpolate average -interpolative-resize 128x128 $i
done


function data = aggregateData(inputData, lats, lngs, hours, cellSize, hourSkip)

latSize = ceil(lats/cellSize);
lngSize = ceil(lngs/cellSize);
locationSize = latSize * lngSize;
timeSlots = ceil(hours/hourSkip);
docSize = locationSize * timeSlots;
data = zeros(docSize, locationSize);

for slat = 1:latSize 
    slat1 = ((slat-1)*cellSize) + 1;
    slat2 = min(slat*cellSize, lats);
    for slng = 1:lngSize
        slng1 = ((slng-1)*cellSize) + 1;
        slng2 = min(slng*cellSize, lngs);
        slocIDs = zeros(1,(slat2 - slat1 + 1)*(slng2 - slng1 + 1));
        c = 1;
        for i = slat1:slat2
            for j = slng1:slng2
                slocIDs(c) = i + ((j-1)*lats);
                c = c + 1;
            end
        end     
        sID = slat + ((slng-1) * latSize);
        for t = 1:timeSlots
            t1 = ((t-1)*hourSkip) + 1;
            t2 = min(t*hourSkip, hours);
            tIDs = t1:t2;
            docID = sID + ((t-1)*locationSize);
            for dlat = 1:latSize
                dlat1 = ((dlat-1)*cellSize) + 1;
                dlat2 = min(dlat*cellSize, lats);
                for dlng = 1:lngSize
                    dlng1 = ((dlng-1)*cellSize) + 1;
                    dlng2 = min(dlng*cellSize, lngs);
                    dlocIDs = zeros(1,(dlat2 - dlat1 + 1)*(dlng2 - dlng1 + 1));
                    c = 1;
                    for i = dlat1:dlat2
                        for j = dlng1:dlng2
                            dlocIDs(c) = i + ((j-1)*lats);
                            c = c + 1;
                        end
                    end
                    dID = dlat + ((dlng-1) * latSize);
                    data(docID, dID) = sum(sum(sum(inputData(slocIDs, dlocIDs, tIDs))));
                end
            end
        end
    end
end

end


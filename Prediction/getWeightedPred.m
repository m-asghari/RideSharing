function p = getWeightedPred(data)

p = zeros(size(data));
for i = 1:size(data, 1)
    rowSum = sum(data(i,:));
    if rowSum == 0
        p(i, :) = 0;
        continue;
    end
    for j = 1:size(data, 2)
        p(i,j) = data(i,j)/rowSum;
    end
end

end


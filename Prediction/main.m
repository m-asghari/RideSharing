lats = 30;
lngs = 15;
hours = 18;
locationSize = lats * lngs;
topicSize = 50;

%[origTrain, origTest] = generateData(topicSize, locationSize, hours, 200, 50);
%save origData.mat origTrain origTest

load origData.mat

cellSizes = 2;
hourSkips = 1:6;

diff = zeros(size(cellSizes, 1), size(hourSkips, 1));
res1 = zeros(size(diff));
res2 = zeros(size(diff));

for cellSize = cellSizes
    for hourSkip = hourSkips
        fprintf('Starting cellSize %d and hourSkip %d\n', cellSize, hourSkip);
        aggTrainData = aggregateData(origTrain, lats, lngs, hours, cellSize, hourSkip);
        aggTestData = aggregateData(origTest, lats, lngs, hours, cellSize, hourSkip);
        
        [pi, theta] = em(aggTrainData, topicSize);
        modelPred = getPrediction(theta, pi);
        
        otherPred = getWeightedPred(aggTrainData);
        
        groundTruth = getWeightedPred(aggTestData);
        
        res1(cellSize, hourSkip) = KLD(groundTruth, modelPred);
        res2(cellSize, hourSkip) = KLD(groundTruth, otherPred);
        diff(cellSize, hourSkip) = res2(cellSize, hourSkip) - res1(cellSize, hourSkip);
        fprintf('MyScore = %.6f, OtherScore = %.6f\n\n', res1(cellSize, hourSkip), res2(cellSize, hourSkip));
    end
end

csvwrite('res1.csv', res1);
csvwrite('res2.csv', res2);
csvwrite('diff.csv', diff);




function [trainData, testData] = generateData(topicSize, locationSize, timeSize, wordPerDocTrain, wordPerDocTest)

topicPriors = zeros(1, topicSize);
for j = 1:topicSize
    topicPriors(j) = (rand()/5) + 0.7;
end
pi = drchrnd(topicPriors, locationSize * timeSize);

thetaPriors = zeros(1, locationSize);
for v = 1:locationSize
    thetaPriors(v) = (rand()*4) + 1;
end
theta = drchrnd(thetaPriors, topicSize);

trainData = zeros(locationSize, locationSize, timeSize);
testData = zeros(locationSize, locationSize, timeSize);
for p = 1:locationSize
    for h = 1:timeSize
        docID = p + ((h-1)*locationSize);
        for l = 1:wordPerDocTrain
            topic = find(mnrnd(1, pi(docID, :)));
            trainData(p, :, h) = trainData(p, :, h) + mnrnd(1, theta(topic, :));
        end
        for l = 1:wordPerDocTest
            topic = find(mnrnd(1, pi(docID, :)));
            testData(p, :, h) = testData(p, :, h) + mnrnd(1, theta(topic, :));
        end
    end
end

end


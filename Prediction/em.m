function [pi, theta] = em(data, topicSize)

docSize = size(data, 1);
wordSize = size(data, 2);

%Initialize pi and theta
topicPriors = zeros(1, topicSize);
for j = 1:topicSize
    topicPriors(j) = (rand()/5) + 0.7;
end
pi = drchrnd(topicPriors, docSize);

thetaPriors = zeros(1, wordSize);
for v = 1:wordSize
    thetaPriors(v) = (rand()*4) + 1;
end
theta = drchrnd(thetaPriors, topicSize);


currentL = logLikelihood(data, theta, pi);
previousL = currentL - 10;
i = 0;
while currentL - previousL > 1.01
    i = i + 1;
    previousL = currentL;
    tau = eStep(data, pi, theta);
    [pi, theta] = mStep(data, tau);
    currentL = logLikelihood(data, theta, pi);
end
end


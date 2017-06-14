function [pi, theta] = mStep(data, tau)

topicSize = length(tau(1, :, 1));
pi = zeros(length(data), topicSize);
for i = 1:length(data)
    denominator = 0;
    for j = 1:topicSize
        for v = 1:length(data(i,:))
            denominator = denominator + (data(i,v)*tau(i,j,v));
        end
    end
    if denominator == 0
        pi(i, :) = 0;
        continue
    end
    for j = 1:topicSize
        nominator = 0;
        for v = 1:length(data(i,:))
            nominator = nominator + (data(i,v)*tau(i,j,v));
        end
        pi(i,j) = nominator / denominator;
    end
end

wordsSize = length(data(1, :));
theta = zeros(topicSize, wordsSize);
for j = 1:topicSize
    denominator = 0;
    for v = 1:wordsSize
        for i = 1:length(data)
            denominator = denominator + (data(i,v) * tau(i, j, v));
        end
    end
    if denominator == 0
        theta(j, :) = 0;
        continue
    end
    for v = 1:wordsSize
        numerator = 0;
        for i = 1:length(data)
            numerator = numerator + (data(i, v) * tau(i, j, v));
        end
        theta(j, v) = numerator / denominator;
    end
end

end


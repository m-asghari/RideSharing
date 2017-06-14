function L = logLikelihood(data, theta, pi)
L = 0;
for i = 1:size(data,1)
    for v = 1:size(data(i,:), 2)
        for j = 1:size(pi(i,:), 2)
            L = L + (data(i, v) * (theta(j, v) * pi(i, j)));
        end
    end
end

end

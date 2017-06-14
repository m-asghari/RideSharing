function tau = eStep(data, pi, theta)

tau = zeros(length(data), length(pi(1,:)), length(data(1,:)));
for i = 1:length(data)
    for v = 1:length(data(i, :))
        denominator = 0;
        for j = 1:length(pi(i,:))
            denominator = denominator + (theta(j,v) * pi(i, j));
        end
        if denominator == 0
            tau(i, :, v) = 0;
            continue
        end
        for j = 1:length(pi(i,:))
            tau(i,j,v) = (theta(j,v) * pi(i,j)) / denominator;
        end
    end
end

end


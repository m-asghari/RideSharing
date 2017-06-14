function p = getPrediction(theta, pi)

n = size(pi, 1);
m = size(theta, 2);
p = zeros(n, m);
for i = 1:n
    for v = 1:m
        sum_ = 0;
        for j = 1:length(pi(i, :))
            sum_ = sum_ + (pi(i,j) * theta(j,v));
        end
        p(i,v) = sum_;
    end
end

end


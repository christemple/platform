package io.fundrequest.platform.github;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GithubSolverHealthCheck implements HealthIndicator {

    private static final String DOWN_PROBLEM_KEY = "problem";

    private final GithubSolverResolver githubSolverResolver;
    private final String owner;
    private final String repo;
    private final Map<String, String> issues;

    public GithubSolverHealthCheck(final GithubSolverResolver githubSolverResolver,
                                   final GithubSolverHealthCheckProperties githubSolverHealthCheckProperties) {
        this.githubSolverResolver = githubSolverResolver;
        this.owner = githubSolverHealthCheckProperties.getOwner();
        this.repo = githubSolverHealthCheckProperties.getRepo();
        this.issues = githubSolverHealthCheckProperties.getIssues();
    }

    @Override
    public Health health() {
        final List<Health> healths = issues.keySet()
                                           .stream()
                                           .map(number -> checkHealth(number, issues.get(number)).withDetail("checkedURL", mapToGithubURL(number)).build())
                                           .collect(Collectors.toList());

        return Health.status(calculateOverallStatus(healths)).withDetail("healths", healths).build();
    }

    private Status calculateOverallStatus(final Collection<Health> healths) {
        final long downAmount = healths.stream()
                                       .map(Health::getStatus)
                                       .filter(Status.DOWN::equals)
                                       .count();
        return downAmount > 0 ? Status.DOWN : Status.UP;
    }

    private Health.Builder checkHealth(final String number, final String expectedSolver) {
        try {

            final Optional<String> solverOptional = githubSolverResolver.resolveSolver(owner, repo, number);
            if (solverOptional.isPresent()) {
                if (expectedSolver.equals(solverOptional.get())) {
                    return Health.up();
                } else {
                    return Health.down()
                                 .withDetail("expectedSolver", expectedSolver)
                                 .withDetail("fetchedSolver", solverOptional.get())
                                 .withDetail(DOWN_PROBLEM_KEY, "Fetched solver does not match expected solver");
                }
            } else {
                return Health.down().withDetail(DOWN_PROBLEM_KEY, "No solver found");
            }
        } catch (Exception e) {
            return Health.down().withDetail(DOWN_PROBLEM_KEY, "Exception thrown while fetching solver");
        }
    }

    private String mapToGithubURL(final String number) {
        return "https://github.com/" + owner + "/" + repo + "/issues/" + number;
    }
}

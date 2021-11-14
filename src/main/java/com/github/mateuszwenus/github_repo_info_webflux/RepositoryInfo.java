package com.github.mateuszwenus.github_repo_info_webflux;

public record RepositoryInfo(String fullName, String description, String cloneUrl, int stars, String createdAt) {

	public RepositoryInfo(GithubResponse input) {
		this(input.getFullName(), input.getDescription(), input.getCloneUrl(), input.getStargazersCount(), input.getCreatedAt());
	}
}

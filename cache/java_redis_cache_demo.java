// ===============================================
// ✅ Java Spring Boot - Redis Caching Demo
// ===============================================

// ---------- 1. Cache-Aside Pattern with RedisTemplate ----------

@Service
public class CacheAsideUserService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public CacheAsideUserService(RedisTemplate<String, Object> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    private static final String CACHE_PREFIX = "user:";

    public User getUserById(Long id) {
        String key = CACHE_PREFIX + id;
        User cached = (User) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
        return user;
    }

    @Transactional
    public void updateUser(Long id, String name) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        userRepository.save(user);

        redisTemplate.delete(CACHE_PREFIX + id);
    }
}

// ---------- 2. Write-Through Caching ----------

@Service
public class WriteThroughUserService {

    @CachePut(value = "user-cache", key = "#user.id")
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}

// ---------- 3. Write-Behind Strategy (Manual Flush) ----------

@Service
public class WriteBehindUserService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WriteBehindUserService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void bufferWrite(User user) {
        String key = "buffer:user:" + user.getId();
        redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(5));
    }

    @Scheduled(fixedRate = 10000)
    public void flushBufferedUsers() {
        // In production: use SCAN command for performance
        Set<String> keys = redisTemplate.keys("buffer:user:*");
        for (String key : keys) {
            User user = (User) redisTemplate.opsForValue().get(key);
            if (user != null) {
                System.out.println("Flushing user to DB: " + user);
                // userRepository.save(user); // actual DB flush
                redisTemplate.delete(key);
            }
        }
    }
}

// ---------- 4. Refresh-Ahead Caching ----------

@Component
public class RefreshAheadTask {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public RefreshAheadTask(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void refreshHotUsers() {
        List<Long> hotUserIds = List.of(1L, 2L, 3L);
        for (Long id : hotUserIds) {
            String key = "user:" + id;
            Duration ttl = redisTemplate.getExpire(key);
            if (ttl != null && ttl.compareTo(Duration.ofSeconds(30)) < 0) {
                User user = userRepository.findById(id).orElse(null);
                if (user != null) {
                    redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
                }
            }
        }
    }
}

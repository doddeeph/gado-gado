package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"time"

	"github.com/redis/go-redis/v9"
)

type User struct {
	ID   int64  `json:"id"`
	Name string `json:"name"`
}

var ctx = context.Background()
var rdb = redis.NewClient(&redis.Options{
	Addr: "localhost:6379",
})

// -------------------- 1. Cache-Aside --------------------
func getUserByID(id int64) (*User, error) {
	key := fmt.Sprintf("user:%d", id)
	val, err := rdb.Get(ctx, key).Result()
	if err == nil {
		var u User
		if err := json.Unmarshal([]byte(val), &u); err == nil {
			return &u, nil
		}
	}

	// Simulate DB fetch
	u := &User{ID: id, Name: "John Doe"}
	buf, _ := json.Marshal(u)
	rdb.Set(ctx, key, buf, 10*time.Minute)
	return u, nil
}

// -------------------- 2. Write-Through --------------------
func updateUserWriteThrough(id int64, name string) error {
	u := &User{ID: id, Name: name}

	// Simulate DB write (should persist to real DB)
	buf, _ := json.Marshal(u)
	key := fmt.Sprintf("user:%d", id)
	return rdb.Set(ctx, key, buf, 10*time.Minute).Err()
}

// -------------------- 3. Write-Behind Buffer --------------------
func writeBehindBuffer(user *User) {
	key := fmt.Sprintf("buffer:user:%d", user.ID)
	buf, _ := json.Marshal(user)
	rdb.Set(ctx, key, buf, 5*time.Minute) // buffer entry
}

func flushWriteBehind() {
	keys, _ := rdb.Keys(ctx, "buffer:user:*").Result()
	for _, key := range keys {
		val, _ := rdb.Get(ctx, key).Result()
		var u User
		json.Unmarshal([]byte(val), &u)
		fmt.Printf("Flushing user to DB: %+v\n", u)
		rdb.Del(ctx, key)
	}
}

// -------------------- 4. Refresh-Ahead TTL --------------------
func refreshAheadHotKeys(hotIDs []int64) {
	for _, id := range hotIDs {
		key := fmt.Sprintf("user:%d", id)
		ttl, _ := rdb.TTL(ctx, key).Result()
		if ttl < 30*time.Second {
			u := &User{ID: id, Name: "Hot User"}
			buf, _ := json.Marshal(u)
			rdb.Set(ctx, key, buf, 10*time.Minute)
		}
	}
}

// -------------------- 5. Benchmark Cache vs DB --------------------
func benchmark(name string, fn func(), iterations int) {
	start := time.Now()
	for i := 0; i < iterations; i++ {
		fn()
	}
	duration := time.Since(start)
	avg := duration / time.Duration(iterations)
	fmt.Printf("%s: total=%s, avg=%s\n", name, duration, avg)
}

func fakeDBFetch() *User {
	time.Sleep(50 * time.Millisecond) // simulate DB latency
	return &User{ID: 999, Name: "DB User"}
}

func fetchFromCache() {
	id := rand.Int63n(10)
	key := fmt.Sprintf("user:%d", id)
	_, err := rdb.Get(ctx, key).Result()
	if err != nil {
		user := fakeDBFetch()
		buf, _ := json.Marshal(user)
		rdb.Set(ctx, key, buf, 10*time.Minute)
	}
}

func fetchDirectDB() {
	_ = fakeDBFetch()
}

func main() {
	rand.Seed(time.Now().UnixNano())
	log.Println("Benchmarking cache vs DB...")

	benchmark("Fetch from Redis (cache-aside)", fetchFromCache, 100)
	benchmark("Fetch from DB only", fetchDirectDB, 100)

	// Normal feature usage
	user, _ := getUserByID(1)
	fmt.Println("Fetched:", user)

	updateUserWriteThrough(1, "Updated Name")
	writeBehindBuffer(&User{ID: 2, Name: "Buffered User"})
	flushWriteBehind()
	refreshAheadHotKeys([]int64{1, 2})
}
